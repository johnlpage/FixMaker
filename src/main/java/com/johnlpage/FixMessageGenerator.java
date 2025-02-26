package com.johnlpage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FixMessageGenerator {

    private static final String SOH = "|"; // FIX standard uses ASCII 1 (Start of Header) as delimiter

    public static void main(String[] args)  {
        if (args.length !=2) {
            System.err.println("Usage: FixMessageGenerator numMessages output-file");
            System.exit(1);
        }
        Random random = new Random(0); // Consistent
        int nmessages = Integer.parseInt(args[0]);
        System.out.println("Generating " + nmessages + " FIX messages as JSON");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {
            for(int i=0;i<nmessages;i++) {
                writer.write(generateFixMessage(random));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String generateFixMessage(Random random) {
        StringBuilder builder = new StringBuilder();
        // Fixed-length headers
        builder.append("{ \"msg\": \"");
        builder.append("8=FIX.4.2").append(SOH); // BeginString

        // Estimate the future size and adjust if necessary, here roughly targeting just below 2000
        int estimatedBodyLength = 1900 + random.nextInt(200);
        builder.append("9=").append(estimatedBodyLength).append(SOH); // BodyLength, approximate
        builder.append("35=D").append(SOH); // MsgType - New Order Single

        // Add real fields for simulation
        builder.append("49=BUYER").append(SOH); // SenderCompID
        builder.append("56=SELLER").append(SOH); // TargetCompID
        builder.append("34=").append(random.nextInt(1000)).append(SOH); // MsgSeqNum
        builder.append("52=").append(currentTimestamp()).append(SOH); // SendingTime

        // Populate with random fields and values to fill up approx 2KB size
        while (builder.length() < 2000 - 7) { // 7 for the CheckSum size at the end.
            builder.append(randomField(random)).append(SOH);
        }

        // Calculate CheckSum - as a simple checksum of all characters
        int checksum = 0;
        for (int i = 0; i < builder.length(); i++) {
            checksum += builder.charAt(i);
        }
        checksum %= 256;
        String checksumStr = String.format("%03d", checksum);

        // Ensure entire FIX message with CheckSum is appended
        builder.append("10=").append(checksumStr).append(SOH);
        builder.append("\"}\n");

        return builder.toString();
    }

    private static String randomField(Random random) {
        int tag = random.nextInt(300) + 50; // Random tag number between 50 and 349
        String value;

        switch (random.nextInt(4)) {
            case 0: // Integer values
                value = String.valueOf(random.nextInt(10000));
                break;
            case 1: // Double values
                value = String.format("%.2f", random.nextDouble() * 1000);
                break;
            case 2: // Timestamp values
                value = currentTimestamp();
                break;
            default: // String values
                value = randomAlphanumeric(random, random.nextInt(10) + 5);
                break;
        }
        return tag + "=" + value;
    }

    private static String randomAlphanumeric(Random random, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String currentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        return dateFormat.format(new Date());
    }
}

