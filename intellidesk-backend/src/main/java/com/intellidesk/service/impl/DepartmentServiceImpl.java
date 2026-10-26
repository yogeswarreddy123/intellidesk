package com.intellidesk.service.impl;

import com.intellidesk.dto.DepartmentDto;
import com.intellidesk.entity.Department;
import com.intellidesk.exception.BadRequestException;
import com.intellidesk.exception.ResourceNotFoundException;
import com.intellidesk.repository.DepartmentRepository;
import com.intellidesk.service.AuditLogService;
import com.intellidesk.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, AuditLogService auditLogService) {
        this.departmentRepository = departmentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return mapToDto(department);
    }

    @Override
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        if (departmentRepository.existsByName(departmentDto.getName())) {
            throw new BadRequestException("Department with name '" + departmentDto.getName() + "' already exists");
        }
        Department department = new Department(departmentDto.getName(), departmentDto.getDescription());
        Department saved = departmentRepository.save(department);

        auditLogService.log("CREATE_DEPARTMENT", "DEPARTMENT", String.valueOf(saved.getId()),
                "Created department: " + saved.getName());

        return mapToDto(saved);
    }

    @Override
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        if (!department.getName().equalsIgnoreCase(departmentDto.getName()) &&
                departmentRepository.existsByName(departmentDto.getName())) {
            throw new BadRequestException("Department with name '" + departmentDto.getName() + "' already exists");
        }

        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        Department updated = departmentRepository.save(department);

        auditLogService.log("UPDATE_DEPARTMENT", "DEPARTMENT", String.valueOf(updated.getId()),
                "Updated department: " + updated.getName());

        return mapToDto(updated);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        departmentRepository.delete(department);

        auditLogService.log("DELETE_DEPARTMENT", "DEPARTMENT", String.valueOf(id),
                "Deleted department: " + department.getName());
    }

    private DepartmentDto mapToDto(Department department) {
        return new DepartmentDto(department.getId(), department.getName(), department.getDescription());
    }
}
