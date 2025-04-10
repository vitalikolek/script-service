package skepn.script.service.repository.admin;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skepn.script.service.model.admin.AdminEmailSettings;

@Repository
public interface AdminEmailSettingsRepository extends JpaRepository<AdminEmailSettings, Long> {

    @CachePut("admin_email_settings")
    @Override
    <T extends AdminEmailSettings> T save(T value);

    @Cacheable("admin_email_settings")
    default AdminEmailSettings findFirst() {
        if (count() == 0) {
            throw new RuntimeException("Admin email settings not found");
        }
        return findAll().get(0);
    }
}
