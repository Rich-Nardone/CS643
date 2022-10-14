
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.regions.Region;
import java.util.*;
import java.io.*;

public class EC2_B {
    public static void main(String[] args) {
        String buck = "njit-cs-643";
        String queue = "queue.fifo";
        SqsClient sqs = SqsClient.builder().region(Region.US_EAST_1).build();
        RekognitionClient Rekognition = RekognitionClient.builder().region(Region.US_EAST_1).build();
        

        process(Rekognition, sqs, buck, queue);
    }

    public static void process(RekognitionClient Rekognition, SqsClient sqs, String buck, String queue) {
        boolean flag = true;
        while (flag) {
            ListQueuesRequest ReqQList = ListQueuesRequest.builder().queueNamePrefix(queue).build();
            ListQueuesResponse ResQList = sqs.listQueues(ReqQList);
            if (ResQList.queueUrls().size() > 0)
                flag = false;
        }
        
        GetQueueUrlRequest getReqQ = GetQueueUrlRequest.builder().queueName(queue).build();
        String url = sqs.getQueueUrl(getReqQ).queueUrl();
 
        flag = true;
        List<String> out = new ArrayList<String>();

        while (flag) {
            ReceiveMessageRequest msgIn = ReceiveMessageRequest.builder().queueUrl(url).maxNumberOfMessages(1).build();
            List<Message> messages = sqs.receiveMessage(msgIn).messages();
            if (messages.size() > 0) {
                Message message = messages.get(0);
                String label = message.body();
                if (label.equals("$")) {
                	flag = false;
                } 
                else {
                    System.out.println("Reading image from queue: " + label);
                    Image img = Image.builder().s3Object(S3Object.builder().bucket(buck).name(label).build()).build();
                    DetectTextRequest req = DetectTextRequest.builder().image(img).build();
                    DetectTextResponse res = Rekognition.detectText(req);
                    List<TextDetection> textDetections = res.textDetections();
                
                    if (textDetections.size()>0){
                        String text = "";
                        for (TextDetection textDetection : textDetections) {
                            if (textDetection.type().equals(TextTypes.WORD))
                                text = text.concat(" " + textDetection.detectedText());
                        }
                        out.add(label+"     : "+text);
                    }
                }
                DeleteMessageRequest delReq = DeleteMessageRequest.builder().queueUrl(url).receiptHandle(message.receiptHandle()).build();
                sqs.deleteMessage(delReq);
        
			}
        }
    	System.out.println("ouput >> output.txt");
		try {
			FileWriter writer = new FileWriter("output.txt");
			writer.write("File Name, Text Found\n");
		    for (int i = 0; i < out.size(); i++)
		        writer.write(out.get(i)+"\n");
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}