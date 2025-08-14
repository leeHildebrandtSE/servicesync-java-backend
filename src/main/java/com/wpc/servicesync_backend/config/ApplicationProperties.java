package com.wpc.servicesync_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String name = "ServiceSync";
    private String version = "1.0.0";
    private String description = "Digital hospital meal delivery tracking system";
    private String timezone = "Africa/Johannesburg";

    private Features features = new Features();
    private Storage storage = new Storage();
    private Email email = new Email();
    private Business business = new Business();

    @Data
    public static class Features {
        private boolean mlOptimization = false;
        private boolean iotIntegration = false;
        private boolean advancedAnalytics = true;
        private boolean realTimeNotifications = true;
        private boolean fileUpload = true;
    }

    @Data
    public static class Storage {
        private String type = "local";
        private Local local = new Local();
        private S3 s3 = new S3();

        @Data
        public static class Local {
            private String uploadDir = "./uploads";
            private String dietSheetDir = "./uploads/diet-sheets";
            private String maxFileSize = "5MB";
        }

        @Data
        public static class S3 {
            private String bucket;
            private String region;
            private String accessKey;
            private String secretKey;
        }
    }

    @Data
    public static class Email {
        private boolean enabled = false;
        private Smtp smtp = new Smtp();
        private String from = "noreply@servicesync.co.za";

        @Data
        public static class Smtp {
            private String host = "smtp.gmail.com";
            private int port = 587;
            private String username;
            private String password;
            private boolean auth = true;
            private boolean starttls = true;
        }
    }

    @Data
    public static class Business {
        private Session session = new Session();
        private Performance performance = new Performance();
        private Alerts alerts = new Alerts();

        @Data
        public static class Session {
            private int autoCompleteHours = 24;
            private int maxMealCount = 100;
            private int minMealCount = 1;
        }

        @Data
        public static class Performance {
            private double targetServingRate = 0.6;
            private int maxTravelTimeMinutes = 15;
            private int maxNurseResponseMinutes = 5;
        }

        @Data
        public static class Alerts {
            private int travelTimeThreshold = 900; // seconds
            private int nurseResponseThreshold = 300; // seconds
            private int completionRateThreshold = 75; // percentage
        }
    }
}