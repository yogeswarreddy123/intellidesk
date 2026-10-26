package com.intellidesk.service.impl;

import com.intellidesk.dto.*;
import com.intellidesk.entity.Department;
import com.intellidesk.entity.Role;
import com.intellidesk.entity.User;
import com.intellidesk.exception.BadRequestException;
import com.intellidesk.exception.ResourceNotFoundException;
import com.intellidesk.exception.UnauthorizedException;
import com.intellidesk.repository.DepartmentRepository;
import com.intellidesk.repository.UserRepository;
import com.intellidesk.security.JwtTokenProvider;
import com.intellidesk.security.UserPrincipal;
import com.intellidesk.service.AuditLogService;
import com.intellidesk.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                            DepartmentRepository departmentRepository,
                            PasswordEncoder passwordEncoder,
                            AuthenticationManager authenticationManager,
                            JwtTokenProvider tokenProvider,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account is deactivated. Please contact an administrator.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        auditLogService.log(user, "LOGIN", "USER", String.valueOf(user.getId()), "User logged in: " + user.getEmail());

        String deptName = user.getDepartment() != null ? user.getDepartment().getName() : null;
        return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), deptName);
    }

    @Override
    public UserDto register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.EMPLOYEE);
        user.setActive(true);

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElse(null);
            user.setDepartment(department);
        }

        User savedUser = userRepository.save(user);

        auditLogService.log(savedUser, "REGISTER", "USER", String.valueOf(savedUser.getId()),
                "New user registered: " + savedUser.getEmail());

        return mapToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User not authenticated");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return getUserById(principal.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return userRepository.searchUsers(search.trim(), pageable).map(this::mapToDto);
        }
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getActiveAgents() {
        return userRepository.findByRoleAndActiveTrue(Role.IT_AGENT).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(UserCreateUpdateDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists: " + email);
        }

        if (!StringUtils.hasText(dto.getPassword()) || dto.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.EMPLOYEE);
        user.setActive(dto.getActive() != null ? dto.getActive() : true);

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            user.setDepartment(department);
        }

        User savedUser = userRepository.save(user);

        auditLogService.log("CREATE_USER", "USER", String.valueOf(savedUser.getId()),
                "Admin created user: " + savedUser.getEmail() + " (" + savedUser.getRole() + ")");

        return mapToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserCreateUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String email = dto.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already in use: " + email);
        }

        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());
        user.setEmail(email);
        user.setPhone(dto.getPhone());

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }

        if (StringUtils.hasText(dto.getPassword())) {
            if (dto.getPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        User updatedUser = userRepository.save(user);

        auditLogService.log("UPDATE_USER", "USER", String.valueOf(updatedUser.getId()),
                "Admin updated user: " + updatedUser.getEmail());

        return mapToDto(updatedUser);
    }

    @Override
    public UserDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);

        auditLogService.log("TOGGLE_USER_STATUS", "USER", String.valueOf(updated.getId()),
                "User active status changed to " + updated.isActive() + " for: " + updated.getEmail());

        return mapToDto(updated);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);

        auditLogService.log("DELETE_USER", "USER", String.valueOf(id),
                "Deleted user: " + user.getEmail());
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }
        return dto;
    }
}
