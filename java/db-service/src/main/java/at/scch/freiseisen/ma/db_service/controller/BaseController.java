package at.scch.freiseisen.ma.db_service.controller;

import at.scch.freiseisen.ma.data_layer.entity.BaseEntity;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import at.scch.freiseisen.ma.db_service.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseController<
        S extends BaseService<R, E, T>,
        R extends BaseRepository<E, T>,
        E extends BaseEntity<T>,
        T extends Serializable> {
    protected final S service;

}
