package com.document.batch.document.batch.repository;

import com.document.batch.document.batch.entity.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    List<Task> findByStatus(String status);
}
