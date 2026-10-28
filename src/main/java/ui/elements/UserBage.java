package ui.elements;

import com.codeborne.selenide.SelenideElement;

public class UserBage extends BaseElement {

    public UserBage(SelenideElement element) {
        super(element);
    }

    public String getUsername() {
        return element.getOwnText().trim();
    }
}
