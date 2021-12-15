package com.eforce21.lib.bin.file.dao;

import com.eforce21.lib.bin.file.entity.BinFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface BinFileRepository extends JpaRepository<BinFileEntity, Long> {


}

