package hiloldasturlari.imagesizechanger;

import hiloldasturlari.imagesizechanger.bot.ImgSizeChangerBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class ImageSizeChangerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageSizeChangerApplication.class, args);

        // Instantiate Telegram Bots API

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new ImgSizeChangerBot());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }


    }
}