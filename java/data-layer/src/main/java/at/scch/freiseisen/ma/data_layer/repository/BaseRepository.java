package at.scch.freiseisen.ma.data_layer.repository;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity<T>, T extends Serializable> extends JpaRepository<E, T> {
}
