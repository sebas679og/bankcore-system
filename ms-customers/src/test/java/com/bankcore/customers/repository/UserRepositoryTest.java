package com.bankcore.customers.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bankcore.customers.AbstractIntegrationTest;
import com.bankcore.customers.DataProvider;
import com.bankcore.customers.model.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

  @Autowired UserRepository userRepository;

  @Test
  void shouldFindById() {
    UserEntity user1 = DataProvider.createMockUser();

    UserEntity savedUser = userRepository.save(user1);

    Optional<UserEntity> found = userRepository.findById(savedUser.getId());

    assertTrue(found.isPresent());
    assertEquals(found.get(), savedUser);
  }

  @Test
  void shouldFindByEmail() {
    UserEntity user1 = DataProvider.createMockUser();

    UserEntity savedUser = userRepository.save(user1);

    Optional<UserEntity> found = userRepository.findByEmailIgnoreCase(savedUser.getEmail());

    assertTrue(found.isPresent());
    assertEquals(found.get(), savedUser);
  }
}
