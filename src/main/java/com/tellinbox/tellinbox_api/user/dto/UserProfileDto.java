package com.tellinbox.tellinbox_api.user.dto;

import com.tellinbox.tellinbox_api.user.model.UserProfileModel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserProfileDto {
    private UUID userId;
    private String publicLink;
    private Boolean receiveAnonymousFeedback;
    private Boolean receiveNamedFeedback;
    private Boolean showStatistics;
    private Boolean showAverageScore;
    private Boolean enableAiAnalysis;
    private Boolean receiveEmailNotifications;
    private Boolean receiveSmsNotifications;
    private Boolean receivePushNotifications;
    private Integer itemsPerPage;
    private String theme;
    private String linkedinUrl;
    private String twitterUrl;
    private String githubUrl;
    private String websiteUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserProfileDto from(UserProfileModel profile) {
        if (profile == null) {
            return null;
        }

        return UserProfileDto.builder()
            .userId(profile.getId())
            .publicLink(profile.getPublicLink())
            .receiveAnonymousFeedback(profile.getReceiveAnonymousFeedback())
            .receiveNamedFeedback(profile.getReceiveNamedFeedback())
            .showStatistics(profile.getShowStatistics())
            .showAverageScore(profile.getShowAverageScore())
            .enableAiAnalysis(profile.getEnableAiAnalysis())
            .receiveEmailNotifications(profile.getReceiveEmailNotifications())
            .receiveSmsNotifications(profile.getReceiveSmsNotifications())
            .receivePushNotifications(profile.getReceivePushNotifications())
            .itemsPerPage(profile.getItemsPerPage())
            .theme(profile.getTheme())
            .linkedinUrl(profile.getLinkedinUrl())
            .twitterUrl(profile.getTwitterUrl())
            .githubUrl(profile.getGithubUrl())
            .websiteUrl(profile.getWebsiteUrl())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }

    public UserProfileModel toEntity() {
        UserProfileModel profile = new UserProfileModel();
        profile.setId(this.userId);
        profile.setPublicLink(this.publicLink);
        profile.setReceiveAnonymousFeedback(this.receiveAnonymousFeedback != null ? this.receiveAnonymousFeedback : true);
        profile.setReceiveNamedFeedback(this.receiveNamedFeedback != null ? this.receiveNamedFeedback : true);
        profile.setShowStatistics(this.showStatistics != null ? this.showStatistics : true);
        profile.setShowAverageScore(this.showAverageScore != null ? this.showAverageScore : true);
        profile.setEnableAiAnalysis(this.enableAiAnalysis != null ? this.enableAiAnalysis : true);
        profile.setReceiveEmailNotifications(this.receiveEmailNotifications != null ? this.receiveEmailNotifications : true);
        profile.setReceiveSmsNotifications(this.receiveSmsNotifications != null ? this.receiveSmsNotifications : true);
        profile.setReceivePushNotifications(this.receivePushNotifications != null ? this.receivePushNotifications : true);
        profile.setItemsPerPage(this.itemsPerPage != null ? this.itemsPerPage : 20);
        profile.setTheme(this.theme != null ? this.theme : "light");
        profile.setLinkedinUrl(this.linkedinUrl);
        profile.setTwitterUrl(this.twitterUrl);
        profile.setGithubUrl(this.githubUrl);
        profile.setWebsiteUrl(this.websiteUrl);
        return profile;
    }
}