package skepn.script.service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import skepn.script.service.model.User;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsernameIgnoreCase(String username);

  Optional<User> findByUsername(String username);

  @Query("SELECT u.regCountryCode, count(u.id) AS registrations FROM User u GROUP BY u.regCountryCode ORDER by registrations DESC")
  List<Object[]> findRegistrationsByCountries();

  default Map<String, Long> findRegistrationsByCountriesAsMap() {
    Map<String, Long> registrations = new LinkedHashMap<>();
    for (Object[] objects : findRegistrationsByCountries()) {
      registrations.put((String) objects[0], (Long) objects[1]);
    }

    return registrations;
  }

  @Query("SELECT u.regCountryCode, count(u.id) AS registrations FROM User u WHERE u.registered >= :startDate GROUP BY u.regCountryCode ORDER by registrations DESC")
  List<Object[]> findRegistrationsByCountries(@Param("startDate") Date startDate);

  default Map<String, Long> findRegistrationsByCountriesAsMap(@Param("startDate") Date startDate) {
    Map<String, Long> registrations = new LinkedHashMap<>();
    for (Object[] objects : findRegistrationsByCountries(startDate)) {
      registrations.put((String) objects[0], (Long) objects[1]);
    }

    return registrations;
  }

  List<User> findAllByLastIpOrRegIpOrderByIdDesc(String lastIp, String regIp);

  List<User> findAllByOrderByLastActivityDesc(Pageable pageable);

  List<User> findAllByLastOnlineGreaterThan(long lastActivity);

  List<User> findAllByLastOnlineGreaterThanOrderByLastActivityDesc(long lastActivity, Pageable pageable);

  boolean existsByUsernameIgnoreCase(String username);

  Long countByLastOnlineGreaterThan(long lastActivity);

  //start admin stats
  @Query("SELECT DATE(u.registered), COUNT(u) " +
          "FROM User u " +
          "GROUP BY DATE(u.registered)")
  List<Object[]> getUsersCountPerDay();

  default Map<Date, Long> getUsersCountPerDayAsMap() {
    Map<Date, Long> map = new LinkedHashMap<>();
    for (Object[] objects : getUsersCountPerDay()) {
      map.put((Date) objects[0], (Long) objects[1]);
    }

    return map;
  }
  //end admin stats
}
