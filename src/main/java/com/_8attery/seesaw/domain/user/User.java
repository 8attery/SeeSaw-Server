package com._8attery.seesaw.domain.user;

import com._8attery.seesaw.domain.battery.Battery;
import com._8attery.seesaw.domain.charge.Charge;
import com._8attery.seesaw.domain.project.Project;
import com._8attery.seesaw.domain.user.provider.Provider;
import com._8attery.seesaw.domain.user.role.Role;
import com._8attery.seesaw.domain.value.Value;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "SS_USER")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "agree_marketing")
    private Boolean agreeMarketing; // 마케팅 약관 동의 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 가입 날짜

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Builder
    public User(Long id, String nickName, String email, Role role, Provider provider, Boolean agreeMarketing) {
        this.id = id;
        this.nickName = nickName;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.agreeMarketing = agreeMarketing;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Value> values = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charge> charge;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Battery battery;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

}
