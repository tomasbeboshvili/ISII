package es.upm.cervezas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    private String firstName;

    private String lastName;

    @Column(length = 500)
    private String intro;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String city;

    private String country;

    @Column(length = 1000)
    private String bio;

    private boolean activated;

    private Instant createdAt;

    private Instant updatedAt;

    private String activationToken;

    private Instant activationTokenCreatedAt;

    private String passwordResetToken;

    private Instant passwordResetExpiresAt;

    private Integer badgeLevel = 0;

    private int gamificationPoints;

    private Long currentAchievementId;
    private int beersCreatedCount = 0;
    private int tastingsCount = 0;
    private int ratingsCount = 0;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (activated) {
            this.activationToken = null;
            this.activationTokenCreatedAt = null;
        }
        if (this.badgeLevel == null) {
            this.badgeLevel = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public Instant getActivationTokenCreatedAt() {
        return activationTokenCreatedAt;
    }

    public void setActivationTokenCreatedAt(Instant activationTokenCreatedAt) {
        this.activationTokenCreatedAt = activationTokenCreatedAt;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
    }

    public Instant getPasswordResetExpiresAt() {
        return passwordResetExpiresAt;
    }

    public void setPasswordResetExpiresAt(Instant passwordResetExpiresAt) {
        this.passwordResetExpiresAt = passwordResetExpiresAt;
    }

    public Integer getBadgeLevel() {
        return badgeLevel;
    }

    public void setBadgeLevel(Integer badgeLevel) {
        this.badgeLevel = badgeLevel;
    }

    public int getGamificationPoints() {
        return gamificationPoints;
    }

    public void setGamificationPoints(int gamificationPoints) {
        this.gamificationPoints = gamificationPoints;
    }

    public Long getCurrentAchievementId() {
        return currentAchievementId;
    }

    public void setCurrentAchievementId(Long currentAchievementId) {
        this.currentAchievementId = currentAchievementId;
    }

    public int getBeersCreatedCount() {
        return beersCreatedCount;
    }

    public void setBeersCreatedCount(int beersCreatedCount) {
        this.beersCreatedCount = beersCreatedCount;
    }

    public int getTastingsCount() {
        return tastingsCount;
    }

    public void setTastingsCount(int tastingsCount) {
        this.tastingsCount = tastingsCount;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }
}
