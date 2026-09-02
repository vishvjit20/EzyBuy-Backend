package com.vj.ezybuy.users.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @ManyToOne
    private User user;

    private Boolean active;
}
