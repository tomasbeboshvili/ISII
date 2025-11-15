package es.upm.cervezas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "tastings")
public class Tasting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beer_id")
    private Beer beer;

    private LocalDateTime tastingDate;

    private String location;

    @Column(length = 2000)
    private String notes;

    private int aromaScore;

    private int flavorScore;

    private int appearanceScore;

    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Beer getBeer() {
        return beer;
    }

    public void setBeer(Beer beer) {
        this.beer = beer;
    }

    public LocalDateTime getTastingDate() {
        return tastingDate;
    }

    public void setTastingDate(LocalDateTime tastingDate) {
        this.tastingDate = tastingDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getAromaScore() {
        return aromaScore;
    }

    public void setAromaScore(int aromaScore) {
        this.aromaScore = aromaScore;
    }

    public int getFlavorScore() {
        return flavorScore;
    }

    public void setFlavorScore(int flavorScore) {
        this.flavorScore = flavorScore;
    }

    public int getAppearanceScore() {
        return appearanceScore;
    }

    public void setAppearanceScore(int appearanceScore) {
        this.appearanceScore = appearanceScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
