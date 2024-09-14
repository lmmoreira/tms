package br.com.logistics.tms.commons.infrastructure.jpa.repositories;

import java.io.Serializable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    <P> Optional<P> findById(ID id, Class<P> projection);
}

