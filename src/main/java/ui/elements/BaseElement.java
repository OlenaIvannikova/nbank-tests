package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

// Представляет отдельный элемент или компонент страницы
public abstract class BaseElement {

    // веб-элемент или компонент страницы
    protected final SelenideElement element;

    public BaseElement(SelenideElement element) {
        this.element = element;
    }

    // Оба метода find ищут один вложенный элемент внутри element

    protected SelenideElement find(By selector) {
        return element.find(selector);
    }

    protected SelenideElement find(String cssSelector) {
        return element.find(cssSelector);
    }

    // Оба метода findAll ищут все вложенные элементы, соответствующие селектору, и возвращают ElementsCollection

    protected ElementsCollection findAll(By selector) {
        return element.findAll(selector);
    }

    protected ElementsCollection findAll(String cssSelector) {
        return element.findAll(cssSelector);
    }
}
