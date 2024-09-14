package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.commons.domain.Id;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Configuration;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.CompanyJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.RelationshipConfigurationJpaJpaRepository;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.projections.Hierarchy;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final RelationshipConfigurationJpaJpaRepository relationshipConfigurationJpaRepository;

    @Override
    public Optional<Company> getCompanyById(CompanyId id) {
        relationshipConfigurationJpaRepository.findById(Id.with("09b6d001-a301-4b61-a951-d50659c5e54d"));
        companyJpaRepository.findById(Id.with("5037d51b-d34b-4f99-ae4d-0d100155202c"));


        final Set<Hierarchy> descendentHierarchy = relationshipConfigurationJpaRepository.findDescendentHierarchyById(id.value());
        Hierarchy entryPoint = descendentHierarchy.stream().filter(it -> it.parentId().equals(id.value()) && it.childId() == null).findFirst().get();
        Company monster = this.getCompany(entryPoint, descendentHierarchy);

   //     final Set<Hierarchy> ascendentHierarchy = relationshipConfigurationJpaRepository.findAscendentHierarchyById(3L);
     //   Hierarchy entryPoint2 = ascendentHierarchy.stream().filter(it -> it.getParentId().equals(3L) && it.getChildId() == null).findFirst().get();
      //  Company correio = this.getCompanyAsc(entryPoint2, ascendentHierarchy);

        return Optional.ofNullable(monster);

    }

    private Company getCompany(Hierarchy entryPoint, Set<Hierarchy> descendentHierarchy) {
        try {
            final Map<String, Map<String, Object>> companyConfiguration = Map.of(
                entryPoint.configurationKey(), entryPoint.getConfigurationValue());

            return
                new Company(new CompanyId(entryPoint.parentId()), entryPoint.parentName(),
                    companyConfiguration.keySet().stream().map(key -> {
                        Map<String, Object> value = companyConfiguration.get(key);
                        return new Configuration(key, value);
                    }).collect(Collectors.toSet()), null, this.getCompanyChildren(entryPoint, descendentHierarchy));

        } catch (Exception ex) {
            return null;
        }
    }

    private Company getCompanyAsc(Hierarchy entryPoint, Set<Hierarchy> ascendentHierarchy) {
        try {
            final Map<String, Map<String, Object>> companyConfiguration = Map.of(
                entryPoint.configurationKey(), entryPoint.getConfigurationValue());

            return
                new Company(new CompanyId(entryPoint.parentId()), entryPoint.parentName(),
                    companyConfiguration.keySet().stream().map(key -> {
                        Map<String, Object> value = companyConfiguration.get(key);
                        return new Configuration(key, value);
                    }).collect(Collectors.toSet()), this.getCompanyParents(entryPoint, ascendentHierarchy), null);

        } catch (Exception ex) {
            return null;
        }
    }

    private Set<Company> getCompanyChildren(Hierarchy entryPoint, Set<Hierarchy> descendentHierarchy) {
        try {

            Set<Hierarchy> childrenHierarchy = descendentHierarchy.stream().filter(it -> Objects.equals(
                it.relationshipConfigurationParentId(), entryPoint.id())).collect(
                Collectors.toSet());

            Set<Company> children = childrenHierarchy.stream().map(child -> {

                final Map<String, Map<String, Object>> childConfig;
                childConfig = Map.of(
                    child.configurationKey(), child.getConfigurationValue());

                return new Company(new CompanyId(child.childId()),
                    child.childName(),
                    childConfig.keySet().stream().map(key -> {
                        Map<String, Object> value = childConfig.get(key);
                        return new Configuration(key, value);
                    }).collect(Collectors.toSet()), null, getCompanyChildren(child,  descendentHierarchy));
            }).collect(Collectors.toSet());
            return children;
        } catch (Exception ex) {
            return null;
        }
    }

    private Set<Company> getCompanyParents(Hierarchy entryPoint, Set<Hierarchy> ascendentHierarchy) {
        try {



            Set<Hierarchy> parentHierarchy = ascendentHierarchy.stream().filter(it -> {
                if (entryPoint.relationshipConfigurationParentId() == null) {
                    return Objects.equals(it.childId(), entryPoint.parentId());
                } else {
                    return Objects.equals(
                        it.id(), entryPoint.relationshipConfigurationParentId());
                }
            }).collect(
                Collectors.toSet());

            Set<Company> parents = parentHierarchy.stream().map(parent -> {

                final Map<String, Map<String, Object>> parentConfig;
                parentConfig = Map.of(
                    parent.configurationKey(),
                        parent.getConfigurationValue());

                return new Company(new CompanyId(parent.parentId()),
                    parent.parentName(),
                    parentConfig.keySet().stream().map(key -> {
                        Map<String, Object> value = parentConfig.get(key);
                        return new Configuration(key, value);
                    }).collect(Collectors.toSet()), getCompanyParents(parent,  ascendentHierarchy), null);
            }).collect(Collectors.toSet());
            return parents;
        } catch (Exception ex) {
            return null;
        }
    }

}
