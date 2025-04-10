package skepn.script.service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.binary.Base32;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import skepn.script.service.util.GeoUtil;
import skepn.script.service.util.MyDecimal;
import skepn.script.service.util.StringUtil;

import java.security.SecureRandom;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Size(min = 5, max = 32)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    @Column(columnDefinition = "VARCHAR(128) DEFAULT ''")
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date registered;

    private boolean twoFactorEnabled;

    @NotBlank
    @Size(max = 32)
    private String twoFactorCode;

    @NotBlank
    @Size(max = 128)
    private String regIp;

    @Column(columnDefinition = "VARCHAR(128) DEFAULT 'N/A'")
    @Size(max = 128)
    private String platform;

    @Column(columnDefinition = "VARCHAR(8) DEFAULT 'NO'")
    private String regCountryCode;

    @Column(columnDefinition = "VARCHAR(8) DEFAULT 'NO'")
    private String lastCountryCode;

    @Size(max = 64)
    private String lastIp;

    private long lastActivity;

    private long lastOnline;

    private int authCount;

    private double verifDepositAmount;

    private long depositsCount;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int smartDepositStep;

    private double deposits;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<UserRole> userRoles = new HashSet<>();

    public User(String username, String password, String regIp, String platform, String countryCode) {
        this.username = username;
        this.password = password;
        this.regIp = regIp;
        this.lastIp = regIp;
        this.lastActivity = System.currentTimeMillis();
        this.lastOnline = this.lastActivity;
        this.platform = platform;
        this.regCountryCode = countryCode;
        this.lastCountryCode = countryCode;
        this.registered = new Date();
        this.twoFactorEnabled = false;
        this.twoFactorCode = generateTwoFactorCode();
    }

    @Transient
    public boolean isAdmin() {
        return this.userRoles.stream().anyMatch(userRole -> userRole.getName() == UserRoleType.ROLE_ADMIN);
    }

    @Transient
    public boolean isWorker() {
        return this.userRoles.stream().anyMatch(userRole -> userRole.getName() == UserRoleType.ROLE_WORKER);
    }

    @Transient
    public boolean isStaff() {
        return isAdmin() || isWorker();
    }

    @Transient
    public String getFormattedLastActivity() {
        long diff = (System.currentTimeMillis() - this.lastActivity) / 1000L;
        if (diff < 60) {
            return diff + " сек. назад";
        } else if (diff > 86400) {
            return StringUtil.formatDate(new Date(this.lastActivity));
        } else if (diff > 3600) {
            return diff / 3600 + "ч. назад";
        } else {
            return diff / 60 + " мин. назад";
        }
    }

    @Transient
    public String getFormattedRegistered() {
        return StringUtil.formatDate(this.registered);
    }

    @Transient
    public String formattedLastActivityEng() {
        return StringUtil.formatDate(new Date(this.lastActivity));
    }

    @Transient
    public GeoUtil.GeoData getGeolocation() {
        return GeoUtil.getGeo(this.lastIp);
    }

    @Transient
    public boolean isOnline() {
        return this.lastOnline >= System.currentTimeMillis() - (10 * 1000);
    }

    @Transient
    public MyDecimal formattedDeposits() {
        return new MyDecimal(this.deposits, true);
    }

    @Transient
    public long getFakeId() {
        long id = this.id + 1127923;
        String idString = String.valueOf(id);
        double multiplier = Double.parseDouble(idString.substring(idString.length() - 3)) / 100D;
        return (long) (id * multiplier);
    }

    private static String generateTwoFactorCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public void addRole(UserRole role) {
        userRoles.add(role);
    }

    public void setRoles(Set<UserRole> roles) {
        this.userRoles = roles;
    }
}
