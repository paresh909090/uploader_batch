package com.document.batch.document.batch.tasklet;

import com.document.batch.document.batch.entity.Document;
import com.document.batch.document.batch.entity.Resource;
import com.document.batch.document.batch.entity.Task;
import com.document.batch.document.batch.parser.FileParser;
import com.document.batch.document.batch.repository.DocumentRepository;
import com.document.batch.document.batch.repository.ResourceRepository;
import com.document.batch.document.batch.repository.TaskRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentsProcessingTasklet implements Tasklet, InitializingBean {

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private FileParser fileParser;
    
    @Value(value = "${resources_base_location}")
	private String resourcesBaseLocation;
    
    @Value(value = "${html_location}")
	private String htmlLocation;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        List<Task> tasks = taskRepository.findByStatus("draft");
        if(tasks != null) {
            tasks.forEach(task -> {
                Resource document = resourceRepository.findById(task.getResourceId()).get();

                if(document != null) {
                    System.out.println(document);
                    //FileParser fileParser = new FileParser();
                    try {
                    	String absoluteDocumentPath = this.resourcesBaseLocation + document.getFilesystemPath();
                        String htmlDocPath = fileParser.convertDocument(absoluteDocumentPath);
                        
                        Document htmlDocument = new Document();
                        htmlDocument.setHtmlDocPath(htmlDocPath.replaceAll(this.htmlLocation, ""));
                        htmlDocument.setResourceId(document.getResourceId());

                        documentRepository.save(htmlDocument);

                        task.setFinalizeDate(new Date());
                        task.setStatus("converted");

                        taskRepository.save(task);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() {

    }
}
