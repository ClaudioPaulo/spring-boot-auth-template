package com.claudiopaulo.userapp.integration;

import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.repository.UserRepository;
import com.claudiopaulo.userapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("User Service Concurrency Tests")
class UserServiceConcurrencyTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Should handle concurrent user registrations")
    void testConcurrentRegistrations() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // All threads start at the same time
                    
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername("user" + index);
                    request.setEmail("user" + index + "@example.com");
                    request.setPassword("password123");
                    
                    userService.registerUser(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // Libera todas as threads
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Then
        assertThat(successCount.get()).isEqualTo(numberOfThreads);
        assertThat(failureCount.get()).isZero();
        assertThat(userRepository.count()).isEqualTo(numberOfThreads);
    }
    
    @Test
    @DisplayName("Should prevent duplicate username in concurrent registrations")
    void testConcurrentDuplicateUsername() throws InterruptedException {
        // Given
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // When - Try to register same username concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // All threads start at the same time
                    
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername("sameuser");
                    request.setEmail("user" + index + "@example.com");
                    request.setPassword("password123");
                    
                    userService.registerUser(request);
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("already exists")) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Other exceptions also count as failures
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown(); // Libera todas as threads
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Then - Only one should succeed
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(numberOfThreads - 1);
        assertThat(userRepository.findByUsername("sameuser")).isPresent();
        assertThat(userRepository.count()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should prevent duplicate email in concurrent registrations")
    void testConcurrentDuplicateEmail() throws InterruptedException {
        // Given
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // When - Try to register same email concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    
                    RegisterRequest request = new RegisterRequest();
                    request.setUsername("user" + index);
                    request.setEmail("same@example.com");
                    request.setPassword("password123");
                    
                    userService.registerUser(request);
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("already exists")) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        startLatch.countDown();
        endLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(numberOfThreads - 1);
        assertThat(userRepository.count()).isEqualTo(1);
    }
}