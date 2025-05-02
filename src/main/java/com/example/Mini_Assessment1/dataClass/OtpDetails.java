package com.example.Mini_Assessment1.dataClass;

    public class OtpDetails {
        public int getOtp() {
            return otp;
        }

        public void setOtp(int otp) {
            this.otp = otp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public OtpDetails(int otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }

        private int otp;
        private long timestamp;


    }

