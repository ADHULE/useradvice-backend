package BlackAdhuleSystem.dev.userAdvicesMariadb.services.implementations;

import BlackAdhuleSystem.dev.userAdvicesMariadb.dto.RoleDto;
import BlackAdhuleSystem.dev.userAdvicesMariadb.entity.Role;
import BlackAdhuleSystem.dev.userAdvicesMariadb.mapper.RoleMapper;
import BlackAdhuleSystem.dev.userAdvicesMariadb.repository.RoleRepository;
import BlackAdhuleSystem.dev.userAdvicesMariadb.services.interfaces.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleDto createRole(RoleDto dto) {
        Role role = RoleMapper.toEntity(dto);
        Role saved = roleRepository.save(role);
        return RoleMapper.toDto(saved);
    }

    @Override
    public List<RoleDto> getRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleMapper::toDto)
                .toList();
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        return RoleMapper.toDto(role);
    }
}
