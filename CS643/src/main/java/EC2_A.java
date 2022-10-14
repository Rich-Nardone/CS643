

import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import java.util.*;

public class  EC2_A{
    public static void main(String[] args) {
        String buck = "njit-cs-643";
        String queue = "queue.fifo"; 
        String queueG = "group1";

        S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();
        SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1).build();
        RekognitionClient rek = RekognitionClient.builder().region(Region.US_EAST_1).build();
        
        process(s3, rek, sqs, buck, queue, queueG);
    }
    public static void process(S3Client s3,RekognitionClient rek, SqsClient sqs, String buck, String queue, String queueG) {
        String url = "";
            ListQueuesRequest QueReq = ListQueuesRequest.builder().queueNamePrefix(queue).build();
            ListQueuesResponse QueRes = sqs.listQueues(QueReq);

            if (QueRes.queueUrls().size() == 0) {
            	Map<String, String> map = new HashMap<>();
            	 
            	map.put("FifoQueue", "true");
            	map.put("ContentBasedDeduplication", "true");
                CreateQueueRequest request = CreateQueueRequest.builder().attributesWithStrings(map).queueName(queue).build();
                sqs.createQueue(request);

                GetQueueUrlRequest getURLQue = GetQueueUrlRequest.builder().queueName(queue).build();
                url = sqs.getQueueUrl(getURLQue).queueUrl();
            } else {
                url = QueRes.queueUrls().get(0);
            }

            ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder().bucket(buck).maxKeys(10).build();
            ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);

            for (S3Object obj : listObjResponse.contents()) {
                System.out.println("Image recieved from S3 Bucket: " + obj.key());

                Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder().bucket(buck).name(obj.key()).build()).build();
                DetectLabelsRequest req = DetectLabelsRequest.builder().image(img).minConfidence((float) 90).build();
                List<Label> labels = rek.detectLabels(req).labels();

                for (Label label : labels) {
                    if (label.name().equals("Car")) {
                        sqs.sendMessage(SendMessageRequest.builder().messageGroupId(queueG).queueUrl(url).messageBody(obj.key()).build());
                        break;
                    }
                }
            }
            sqs.sendMessage(SendMessageRequest.builder().queueUrl(url).messageGroupId(queueG).messageBody("$").build());
    }
}