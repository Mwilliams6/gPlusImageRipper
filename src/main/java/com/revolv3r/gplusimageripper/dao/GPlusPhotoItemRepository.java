package com.revolv3r.gplusimageripper.dao;

import com.revolv3r.gplusimageripper.domain.GooglePlusPhoto;
import org.springframework.data.repository.CrudRepository;

public interface GPlusPhotoItemRepository extends CrudRepository<GooglePlusPhoto, Integer>
{

}
