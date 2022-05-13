package io.oodles.transcribe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.oodles.transcribe.service.TranscribeServiceImpl;

@RestController
public class TranscribeController {
	@Autowired
	TranscribeServiceImpl transcribeServiceImpl; 
	
	@PostMapping("/process")
	public ResponseEntity<?> transcribeUsingVideoUrl(@RequestParam String videoUrl){
		
		String resultUrl = transcribeServiceImpl.transcriptionProcess(videoUrl);
		if(resultUrl!=null) {
				return new ResponseEntity<>( resultUrl , HttpStatus.OK );
			}
		return new ResponseEntity<>("FAILED", HttpStatus.EXPECTATION_FAILED);

		
	}
	
	@GetMapping("/download")
	public ResponseEntity<ByteArrayResource> downloadTranscriptionFile(@RequestParam String fileUrl){
		byte[] data = transcribeServiceImpl.downloadFile(fileUrl);
		ByteArrayResource resource =  new ByteArrayResource(data);
		return  ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + fileUrl + "\"")
                .body(resource);
	}
	
}
