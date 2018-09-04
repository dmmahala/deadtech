package com.deadtech.deadtech.models.data;

import com.deadtech.deadtech.models.Manufacturer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;

@Repository
@Transactional
public interface ManufacturerDao extends CrudRepository<Manufacturer, Integer> {
}
