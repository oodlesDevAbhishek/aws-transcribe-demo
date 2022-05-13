package io.oodles.transcribe.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.LanguageCode;
import com.amazonaws.services.transcribe.model.Media;
import com.amazonaws.services.transcribe.model.Settings;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.StartTranscriptionJobResult;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;
import com.amazonaws.util.IOUtils;

@Service
public class TranscribeServiceImpl {

	@Value("${cloud.aws.bucket.name}")
	public String bucketName;
	@Autowired
	public AmazonS3 s3Clinet;
	@Autowired
	public AmazonTranscribe transcribeClinet;

	public String transcriptionProcess(String videoLink) {
		String jobName = System.currentTimeMillis() + "_" + "video.mp4";
		// Start Transcription Job and get result
		StartTranscriptionJobResult startTranscriptionJobResult = startTranscriptionJob(videoLink, jobName);
		System.out.println(startTranscriptionJobResult);

		// Get result after the processing is complete
		GetTranscriptionJobResult getTranscriptionJobResult = getTranscriptionJobResult(jobName);
		return getTranscriptionJobResult.toString();
	}

	private StartTranscriptionJobResult startTranscriptionJob(String videoLink, String jobName) {
		Media media = new Media();
		media.setMediaFileUri(videoLink);

		// Create the transcription job request

		StartTranscriptionJobRequest request = new StartTranscriptionJobRequest();
		request.withLanguageCode(LanguageCode.EnUS);
		request.withMedia(media);
		request.setTranscriptionJobName(jobName);
		request.withMediaFormat("mp4");
		request.setOutputBucketName(bucketName);
		request.setSubtitles(null);
		Settings settings = new Settings();
		settings.withMaxSpeakerLabels(10).withShowSpeakerLabels(true);
		request.withSettings(settings);

		// send the request to start the transcription job
		StartTranscriptionJobResult startTranscriptionJobResult = transcribeClinet.startTranscriptionJob(request);
		System.out.println("Created the transcription job");
		return startTranscriptionJobResult;
	}

	private GetTranscriptionJobResult getTranscriptionJobResult(String transcriptionJobName) {
		Boolean resultFound = false;
		TranscriptionJob transcriptionJob = new TranscriptionJob();
		GetTranscriptionJobRequest getTranscriptionJobRequest = new GetTranscriptionJobRequest()
				.withTranscriptionJobName(transcriptionJobName);
		GetTranscriptionJobResult getTranscriptionJobResult = new GetTranscriptionJobResult();
		while (resultFound == false) {
			getTranscriptionJobResult = transcribeClinet.getTranscriptionJob(getTranscriptionJobRequest);
			transcriptionJob = getTranscriptionJobResult.getTranscriptionJob();

			if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.COMPLETED.name())) {
				return getTranscriptionJobResult;
			} else if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.FAILED.name())) {
				return null;
			} else if (transcriptionJob.getTranscriptionJobStatus()
					.equalsIgnoreCase(TranscriptionJobStatus.IN_PROGRESS.name())) {
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					System.out.println("Interrupted Exception {}" + e.getMessage());
				}
			}

		}
		return getTranscriptionJobResult;
	}
	
    public byte[] downloadFile(String fileUrl) {
    	String S3TranscriptionFileName = fileUrl.split("s3.ap-south-1.amazonaws.com/transcription-output-file/")[1];
    	S3Object s3Object = s3Clinet.getObject(bucketName, S3TranscriptionFileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	

}
