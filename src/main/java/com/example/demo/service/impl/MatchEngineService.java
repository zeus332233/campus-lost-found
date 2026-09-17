package com.example.demo.service.impl;

import com.example.demo.mapper.FoundItemMapper;
import com.example.demo.mapper.LostItemMapper;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.FoundItem;
import com.example.demo.model.LostItem;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能匹配引擎
 * 当发布失物招领时，自动匹配对应的寻物启事；反之亦然
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchEngineService {

    private final FoundItemMapper foundItemMapper;
    private final LostItemMapper lostItemMapper;
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    /** 匹配阈值：分数超过此值才认为匹配 */
    private static final int MATCH_THRESHOLD = 50;
    /** 地点相似度阈值 */
    private static final double LOCATION_SIMILARITY_THRESHOLD = 0.3;

    /**
     * 失物招领发布后，在寻物启事中查找匹配项
     */
    public List<MatchResult> findMatchesForFoundItem(FoundItem foundItem) {
        List<LostItem> candidates = lostItemMapper.selectByCategoryAndStatus(
                foundItem.getCategory(), "SEARCHING");
        List<MatchResult> results = new ArrayList<>();

        for (LostItem candidate : candidates) {
            // 排除同一用户自己发布的
            if (candidate.getUserId().equals(foundItem.getUserId())) {
                continue;
            }
            int score = calculateMatchScore(foundItem, candidate);
            if (score >= MATCH_THRESHOLD) {
                results.add(new MatchResult("LOST", candidate.getId(), score,
                        candidate.getTitle(), candidate.getUserId()));
            }
        }

        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return results;
    }

    /**
     * 寻物启事发布后，在失物招领中查找匹配项
     */
    public List<MatchResult> findMatchesForLostItem(LostItem lostItem) {
        List<FoundItem> candidates = foundItemMapper.selectByCategoryAndStatus(
                lostItem.getCategory(), "SEARCHING");
        List<MatchResult> results = new ArrayList<>();

        for (FoundItem candidate : candidates) {
            if (candidate.getUserId().equals(lostItem.getUserId())) {
                continue;
            }
            int score = calculateMatchScore(candidate, lostItem);
            if (score >= MATCH_THRESHOLD) {
                results.add(new MatchResult("FOUND", candidate.getId(), score,
                        candidate.getTitle(), candidate.getUserId()));
            }
        }

        results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return results;
    }

    /**
     * 计算失物招领与寻物启事的匹配分数
     */
    public int calculateMatchScore(FoundItem foundItem, LostItem lostItem) {
        int score = 0;

        // 1. 类别匹配（完全相同 30分）
        if (foundItem.getCategory() != null && foundItem.getCategory().equals(lostItem.getCategory())) {
            score += 30;
        }

        // 2. 地点相似度（最高 30分）
        int locationScore = calculateLocationSimilarity(
                foundItem.getLocation(), lostItem.getLocation());
        score += locationScore;

        // 3. 时间接近度（最高 40分）
        int timeScore = calculateTimeProximity(foundItem.getFoundTime(), lostItem.getLostTime());
        score += timeScore;

        // 4. 标题关键词匹配加分（最高 20分）
        int titleScore = calculateTitleKeywordMatch(foundItem.getTitle(), lostItem.getTitle());
        score += Math.min(titleScore, 20);

        return Math.min(score, 100);
    }

    /**
     * 地点相似度评分：0-30分
     */
    private int calculateLocationSimilarity(String loc1, String loc2) {
        if (loc1 == null || loc2 == null || loc1.isEmpty() || loc2.isEmpty()) {
            return 0;
        }
        // 完全相同
        if (loc1.equals(loc2)) return 30;
        // 互相包含
        if (loc1.contains(loc2) || loc2.contains(loc1)) return 25;

        // 按字符拆分后比较共同片段
        Set<Character> set1 = new HashSet<>();
        Set<Character> set2 = new HashSet<>();
        for (char c : loc1.toCharArray()) set1.add(c);
        for (char c : loc2.toCharArray()) set2.add(c);
        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);

        double similarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        if (similarity >= LOCATION_SIMILARITY_THRESHOLD) {
            return (int) (similarity * 30);
        }
        return 0;
    }

    /**
     * 时间接近度评分：0-40分
     * 7天以内给分，越接近分数越高
     */
    private int calculateTimeProximity(Long foundTime, Long lostTime) {
        if (foundTime == null || lostTime == null) return 10;

        long diff = Math.abs(foundTime - lostTime);
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;

        if (diff <= sevenDaysMs) {
            // 时间差越小分数越高
            double ratio = 1.0 - ((double) diff / sevenDaysMs);
            return (int) (ratio * 40);
        }
        // 超过7天但不超过30天，给少量分
        long thirtyDaysMs = 30L * 24 * 60 * 60 * 1000;
        if (diff <= thirtyDaysMs) {
            return 10;
        }
        return 0;
    }

    /**
     * 标题关键词匹配评分：0-20分
     */
    private int calculateTitleKeywordMatch(String title1, String title2) {
        if (title1 == null || title2 == null || title1.isEmpty() || title2.isEmpty()) {
            return 0;
        }
        // 简单分词（按常见分隔符）
        String[] words1 = title1.split("[，,、\\s]+");
        String[] words2 = title2.split("[，,、\\s]+");

        int matchCount = 0;
        for (String w1 : words1) {
            if (w1.length() < 2) continue; // 忽略单字
            for (String w2 : words2) {
                if (w1.equals(w2) || w1.contains(w2) || w2.contains(w1)) {
                    matchCount++;
                    break;
                }
            }
        }
        int maxLen = Math.max(words1.length, words2.length);
        return maxLen == 0 ? 0 : (int) ((double) matchCount / maxLen * 20);
    }

    /**
     * 发送匹配通知给双方用户
     */
    public void notifyMatch(FoundItem foundItem, LostItem lostItem, int score) {
        long now = System.currentTimeMillis();

        // 通知失物招领发布者
        User foundPublisher = userMapper.selectById(foundItem.getUserId());
        if (foundPublisher != null) {
            Notification notif = new Notification();
            notif.setUserId(foundPublisher.getId());
            notif.setType("MATCH_FOUND");
            notif.setTitle("发现可能匹配的寻物启事");
            notif.setContent("您发布的失物招领「" + foundItem.getTitle()
                    + "」可能与寻物启事「" + lostItem.getTitle()
                    + "」匹配（匹配度：" + score + "%），请查看确认。");
            notif.setRelatedId(lostItem.getId());
            notif.setCreateTime(now);
            notif.setIsRead(0);
            notificationMapper.insert(notif);
            log.info("匹配通知已发送: 失物方用户={} -> 寻物ID={}，匹配度={}%",
                    foundPublisher.getId(), lostItem.getId(), score);
        }

        // 通知寻物启事发布者
        User lostPublisher = userMapper.selectById(lostItem.getUserId());
        if (lostPublisher != null) {
            Notification notif = new Notification();
            notif.setUserId(lostPublisher.getId());
            notif.setType("MATCH_FOUND");
            notif.setTitle("发现可能匹配的失物招领");
            notif.setContent("您发布的寻物启事「" + lostItem.getTitle()
                    + "」可能与失物招领「" + foundItem.getTitle()
                    + "」匹配（匹配度：" + score + "%），请查看确认。");
            notif.setRelatedId(foundItem.getId());
            notif.setCreateTime(now);
            notif.setIsRead(0);
            notificationMapper.insert(notif);
            log.info("匹配通知已发送: 寻物方用户={} -> 失物ID={}，匹配度={}%",
                    lostPublisher.getId(), foundItem.getId(), score);
        }
    }

    /**
     * 匹配结果 DTO
     */
    public static class MatchResult {
        private String itemType;    // FOUND 或 LOST
        private Long itemId;
        private int score;          // 匹配分数 0-100
        private String itemTitle;
        private Long userId;        // 被匹配物品的发布者ID

        public MatchResult(String itemType, Long itemId, int score, String itemTitle, Long userId) {
            this.itemType = itemType;
            this.itemId = itemId;
            this.score = score;
            this.itemTitle = itemTitle;
            this.userId = userId;
        }

        public String getItemType() { return itemType; }
        public Long getItemId() { return itemId; }
        public int getScore() { return score; }
        public String getItemTitle() { return itemTitle; }
        public Long getUserId() { return userId; }
    }
}
