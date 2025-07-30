package io.github.rosestack.example;

import io.github.rosestack.mybatis.support.encryption.HashService;
import io.github.rosestack.mybatis.support.encryption.HashType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户查询服务测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private HashService hashService;

    @InjectMocks
    private UserQueryService userQueryService;

    private UserEntity testUser;
    private final String testPhone = "13800138000";
    private final String testEmail = "admin@example.com";
    private final String testPhoneHash = "phone_hash_value";
    private final String testEmailHash = "email_hash_value";

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPhone(testPhone);
        testUser.setEmail(testEmail);
        testUser.setPhoneHash(testPhoneHash);
        testUser.setEmailHash(testEmailHash);
    }

    @Test
    void testFindByPhone_Success() {
        // Given
        when(hashService.generateHashByField(testPhone, UserEntity.class, "phone")).thenReturn(testPhoneHash);
        when(userMapper.findByPhoneHash(testPhoneHash)).thenReturn(testUser);

        // When
        UserEntity result = userQueryService.findByPhone(testPhone);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(hashService).generateHashByField(testPhone, UserEntity.class, "phone");
        verify(userMapper).findByPhoneHash(testPhoneHash);
    }

    @Test
    void testFindByPhone_NotFound() {
        // Given
        when(hashService.generateHashByField(testPhone, UserEntity.class, "phone")).thenReturn(testPhoneHash);
        when(userMapper.findByPhoneHash(testPhoneHash)).thenReturn(null);

        // When
        UserEntity result = userQueryService.findByPhone(testPhone);

        // Then
        assertNull(result);

        verify(hashService).generateHashByField(testPhone, UserEntity.class, "phone");
        verify(userMapper).findByPhoneHash(testPhoneHash);
    }

    @Test
    void testFindByEmail_Success() {
        // Given
        when(hashService.generateHashByField(testEmail, UserEntity.class, "email")).thenReturn(testEmailHash);
        when(userMapper.findByEmailHash(testEmailHash)).thenReturn(testUser);

        // When
        UserEntity result = userQueryService.findByEmail(testEmail);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        verify(hashService).generateHashByField(testEmail, UserEntity.class, "email");
        verify(userMapper).findByEmailHash(testEmailHash);
    }

    @Test
    void testFindByEmail_NotFound() {
        // Given
        when(hashService.generateHashByField(testEmail, UserEntity.class, "email")).thenReturn(testEmailHash);
        when(userMapper.findByEmailHash(testEmailHash)).thenReturn(null);

        // When
        UserEntity result = userQueryService.findByEmail(testEmail);

        // Then
        assertNull(result);

        verify(hashService).generateHashByField(testEmail, UserEntity.class, "email");
        verify(userMapper).findByEmailHash(testEmailHash);
    }

    @Test
    void testFindByPhoneOrEmail_Success() {
        // Given
        String phoneOrEmail = testPhone;
        when(hashService.generateHashByField(phoneOrEmail, UserEntity.class, "phone"))
                .thenReturn(testPhoneHash);
        when(hashService.generateHashByField(phoneOrEmail, UserEntity.class, "email"))
                .thenReturn(testEmailHash);
        when(userMapper.findByPhoneHashOrEmailHash(testPhoneHash, testEmailHash))
                .thenReturn(Arrays.asList(testUser));

        // When
        List<UserEntity> result = userQueryService.findByPhoneOrEmail(phoneOrEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());

        verify(hashService).generateHashByField(phoneOrEmail, UserEntity.class, "phone");
        verify(hashService).generateHashByField(phoneOrEmail, UserEntity.class, "email");
        verify(userMapper).findByPhoneHashOrEmailHash(testPhoneHash, testEmailHash);
    }

    @Test
    void testFindByPhoneOrEmail_EmptyResult() {
        // Given
        String phoneOrEmail = "nonexistent@example.com";
        String nonexistentPhoneHash = "nonexistent_phone_hash";
        String nonexistentEmailHash = "nonexistent_email_hash";
        when(hashService.generateHashByField(phoneOrEmail, UserEntity.class, "phone"))
                .thenReturn(nonexistentPhoneHash);
        when(hashService.generateHashByField(phoneOrEmail, UserEntity.class, "email"))
                .thenReturn(nonexistentEmailHash);
        when(userMapper.findByPhoneHashOrEmailHash(nonexistentPhoneHash, nonexistentEmailHash))
                .thenReturn(Collections.emptyList());

        // When
        List<UserEntity> result = userQueryService.findByPhoneOrEmail(phoneOrEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(hashService).generateHashByField(phoneOrEmail, UserEntity.class, "phone");
        verify(hashService).generateHashByField(phoneOrEmail, UserEntity.class, "email");
        verify(userMapper).findByPhoneHashOrEmailHash(nonexistentPhoneHash, nonexistentEmailHash);
    }

    @Test
    void testFindByPhones_Success() {
        // Given
        List<String> phones = Arrays.asList("13800138000", "13800138001");
        List<String> phoneHashes = Arrays.asList("hash1", "hash2");
        List<UserEntity> users = Arrays.asList(testUser);

        when(hashService.generateHashByField("13800138000", UserEntity.class, "phone")).thenReturn("hash1");
        when(hashService.generateHashByField("13800138001", UserEntity.class, "phone")).thenReturn("hash2");
        when(userMapper.findByPhoneHashes(phoneHashes)).thenReturn(users);

        // When
        List<UserEntity> result = userQueryService.findByPhones(phones);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());

        verify(hashService).generateHashByField("13800138000", UserEntity.class, "phone");
        verify(hashService).generateHashByField("13800138001", UserEntity.class, "phone");
        verify(userMapper).findByPhoneHashes(phoneHashes);
    }

    @Test
    void testVerifyPhone_Success() {
        // Given
        when(hashService.generateHashByField(testPhone, UserEntity.class, "phone"))
                .thenReturn(testPhoneHash);
        when(hashService.constantTimeEquals(testPhoneHash, testPhoneHash))
                .thenReturn(true);

        // When
        boolean result = userQueryService.verifyPhone(testPhone, testPhoneHash);

        // Then
        assertTrue(result);
        verify(hashService).generateHashByField(testPhone, UserEntity.class, "phone");
        verify(hashService).constantTimeEquals(testPhoneHash, testPhoneHash);
    }

    @Test
    void testVerifyPhone_Failed() {
        // Given
        String wrongPhone = "13800138001";
        String wrongPhoneHash = "wrong_phone_hash";
        when(hashService.generateHashByField(wrongPhone, UserEntity.class, "phone"))
                .thenReturn(wrongPhoneHash);
        when(hashService.constantTimeEquals(wrongPhoneHash, testPhoneHash))
                .thenReturn(false);

        // When
        boolean result = userQueryService.verifyPhone(wrongPhone, testPhoneHash);

        // Then
        assertFalse(result);
        verify(hashService).generateHashByField(wrongPhone, UserEntity.class, "phone");
        verify(hashService).constantTimeEquals(wrongPhoneHash, testPhoneHash);
    }

    @Test
    void testVerifyEmail_Success() {
        // Given
        when(hashService.generateHashByField(testEmail, UserEntity.class, "email"))
                .thenReturn(testEmailHash);
        when(hashService.constantTimeEquals(testEmailHash, testEmailHash))
                .thenReturn(true);

        // When
        boolean result = userQueryService.verifyEmail(testEmail, testEmailHash);

        // Then
        assertTrue(result);
        verify(hashService).generateHashByField(testEmail, UserEntity.class, "email");
        verify(hashService).constantTimeEquals(testEmailHash, testEmailHash);
    }

    @Test
    void testSaveUser_Success() {
        // Given
        when(userMapper.insert(testUser)).thenReturn(1);

        // When
        UserEntity result = userQueryService.saveUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userMapper).insert(testUser);
    }

    @Test
    void testSaveUser_Failed() {
        // Given
        when(userMapper.insert(testUser)).thenReturn(0);

        // When
        UserEntity result = userQueryService.saveUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userMapper).insert(testUser);
    }
}
