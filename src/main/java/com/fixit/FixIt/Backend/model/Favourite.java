package com.fixit.FixIt.Backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favourites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favourite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonBackReference
    private User provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    // Optional: Add createdAt and updatedAt timestamps if needed
    // @Column(name = "created_at")
    // @Temporal(TemporalType.TIMESTAMP)
    // private java.util.Date createdAt;

    // @Column(name = "updated_at")
    // @Temporal(TemporalType.TIMESTAMP)
    // private java.util.Date updatedAt;

    // @PrePersist
    // protected void onCreate() {
    //     createdAt = new java.util.Date();
    //     updatedAt = new java.util.Date();
    // }

    // @PreUpdate
    // protected void onUpdate() {
    //     updatedAt = new java.util.Date();
    // }
} 