package hiloldasturlari.imagesizechanger.bot;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;


public class ImgSizeChangerBot extends TelegramLongPollingBot {


    @Value("${}")
    private String botUsername;
    private String botToken;



    @Override
    public String getBotUsername() {
        return "imgsizechanger_bot";
    }

    @Override
    public String getBotToken() {
        return "7474398004:AAGk2Ykki13nkxY08_f7hbj1dhdOxMHFjSM";
    }


    private enum State { AWAITING_IMAGE, AWAITING_DIMENSIONS }
    private State state = State.AWAITING_IMAGE;
    private String lastFileId;
    private boolean lastFileCompressed;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (state == State.AWAITING_IMAGE) {
                if (update.getMessage().hasPhoto()) {
                    handlePhoto(update);
                } else if (update.getMessage().hasDocument()) {
                    handleDocument(update);
                } else {
                    sendTextMessage(update.getMessage().getChatId(), "Iltimos faqat rasm yuboring!");
                }
            } else if (state == State.AWAITING_DIMENSIONS) {
                if (update.getMessage().hasText()) {
                    String[] dimensions = update.getMessage().getText().split(" ");
                    if (dimensions.length == 2) {
                        try {
                            int width = Integer.parseInt(dimensions[0]);
                            int height = Integer.parseInt(dimensions[1]);
                            resizeAndSendPhoto(lastFileId, update.getMessage().getChatId(), width, height, lastFileCompressed);
                            state = State.AWAITING_IMAGE;
                        } catch (NumberFormatException e) {
                            sendTextMessage(update.getMessage().getChatId(), "Invalid dimensions. Please send the width and height as two integers separated by a space.");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        sendTextMessage(update.getMessage().getChatId(), "Iltimos kerakli o'lchamlarni probel orqali ajratib kerakli o'lchamni tanlang.");
                    }
                }
            }
        }
    }

    private void handlePhoto(Update update) {
        Optional<PhotoSize> largestPhoto = update.getMessage().getPhoto().stream()
                .max((p1, p2) -> p1.getFileSize().compareTo(p2.getFileSize()));

        if (largestPhoto.isPresent()) {
            lastFileId = largestPhoto.get().getFileId();
            lastFileCompressed = true;
            sendTextMessage(update.getMessage().getChatId(), "Iltimos kerakli o'lchamlarni probel orqali ajratib kerakli o'lchamni tanlang.");
            state = State.AWAITING_DIMENSIONS;
        }
    }

    private void handleDocument(Update update) {
        Document document = update.getMessage().getDocument();
        if (document.getMimeType().equals("image/jpeg")) {
            lastFileId = document.getFileId();
            lastFileCompressed = false;
            sendTextMessage(update.getMessage().getChatId(), "Iltimos kerakli o'lchamlarni probel orqali ajratib kerakli o'lchamni tanlang.");
            state = State.AWAITING_DIMENSIONS;
        }
    }

    private void resizeAndSendPhoto(String fileId, Long chatId, int width, int height, boolean compressed) throws Exception {
        File file = getFileFromTelegram(fileId);
        String fileUrl = file.getFileUrl(getBotToken());
        InputStream inputStream = new URL(fileUrl).openStream();
        byte[] fileBytes = IOUtils.toByteArray(inputStream);

        BufferedImage resizedImage = resizeImage(new ByteArrayInputStream(fileBytes), width, height);
        java.io.File outputFile = new java.io.File(compressed ? "resized_image_compressed.jpg" : "resized_image.jpg");
        ImageIO.write(resizedImage, compressed ? "jpg" : "jpeg", outputFile);


        InputFile inputFile = new InputFile();
        inputFile.setMedia(outputFile);
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId.toString());
        sendPhotoRequest.setPhoto(inputFile);
        execute(sendPhotoRequest);
    }

    private File getFileFromTelegram(String fileId) throws TelegramApiException {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(fileId);
        return execute(getFileMethod);
    }

    private BufferedImage resizeImage(InputStream inputStream, int width, int height) throws Exception {
        BufferedImage originalImage = ImageIO.read(inputStream);
        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, null);
        g2d.dispose();

        return newImage;
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
