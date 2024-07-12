package hiloldasturlari.imagesizechanger.service.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.objects.Update;

public class ImgSizeChangerBot extends TelegramLongPollingBot {
    @Override
    public String getBotUsername() {
        return "imgsizechanger_bot";
    }

    @Override
    public String getBotToken() {
        return "7474398004:AAGk2Ykki13nkxY08_f7hbj1dhdOxMHFjSM";
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

}
