package com.example.spotifyclone.entity;

import com.example.spotifyclone.enums.ROLE;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private ROLE role;


    @Column(name = "is_verified")
    @Builder.Default
    private boolean isVerified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expiration")
    private LocalDateTime verificationCodeExpiration;


    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<Idea> ideas = new ArrayList<>();

    /*@Column(name = "avatar_url")
    private String profilePictureUrl;

    @Column(name = "avatar_public_id")
    private String profilePicturePublicId;*/



    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();


    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth", updatable = false/*, insertable = false*/)
    private LocalDate dateOfBirth;


    @Column(name = "two_factor_email")
    @Builder.Default
    private Boolean twoFactorEmail = false;


   /* @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Profile profile;*/
   @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
   @JoinColumn(name = "avatar_id")
   private Image avatarId;


    public static boolean isAdult(LocalDate birthDate) {
        return birthDate.plusYears(18).isBefore(LocalDate.now()) ||
               birthDate.plusYears(18).equals(LocalDate.now());
    }
}