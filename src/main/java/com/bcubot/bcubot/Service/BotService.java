package com.bcubot.bcubot.Service;

import com.bcubot.bcubot.model.Client;
import com.bcubot.bcubot.model.Result;
import com.bcubot.bcubot.util.WebDriverSingleton;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BotService {
    private final WebDriverSingleton driver;
    private String bookedSeat;
    private Client client;
    private Result result;

    public BotService(WebDriverSingleton driver) {
        this.bookedSeat = "";
        this.driver = driver;
        this.result = Result.ERROR;
    }

    public String getBookedSeat() {
        return bookedSeat;
    }

    private Result delete() {
        while (true) {
            try {
                this.driver.getDriver().findElement(By.cssSelector("a[title='Informaţii despre contul personal']")).click();
                Thread.sleep(100);

                if (this.driver.getDriver().findElement(By.xpath("//a[contains(text(), '1')]")) == null) {
                    return Result.NO_BOOKING;
                }

                this.driver.getDriver().findElement(By.xpath("//a[contains(text(), '1')]")).click();
                Thread.sleep(100);

                if (this.driver.getDriver().getPageSource().contains("Nu aveţi nici o cerere de împrumut interbibliotecar în aşteptare.")) {
                    return Result.NO_BOOKING;
                }

                this.driver.getDriver().findElement(By.xpath("//a[contains(text(), '1')]")).click();
                Thread.sleep(100);


                this.driver.getDriver().findElement(By.cssSelector("img[src=\"http://aleph.bcucluj.ro:8991/exlibris/aleph/u23_1/alephe/www_f_rum/icon/f-delete.gif\"]")).click();
                this.result = Result.DELETED;
                return this.result;
            } catch (Exception e) {
                trafficError();
            }
        }
    }

    public Result deleteBooking(Client client) throws InterruptedException {
        this.driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//        clear login and other cookies
        this.driver.getDriver().manage().deleteAllCookies();

        this.client = client;

        if (connect() == Result.OK) {
            Thread.sleep(500);
            if (login() == Result.OK) {
                Thread.sleep(500);
                this.result = delete();
            }
        }

        return this.result;
    }

    public Result startBooking(Client client) throws InterruptedException {
        this.driver.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//            // clear login and other cookies
        this.driver.getDriver().manage().deleteAllCookies();

        this.client = client;

        if (connect() == Result.OK) {
            Thread.sleep(500);
            if (login() == Result.OK) {
                Thread.sleep(500);
                if (seats() == Result.OK) {
                    Thread.sleep(500);
                    this.result = getSeatList();
                }
            }
        }

        return this.result;
    }

    private Result connect() {
        while (true) {
            try {
                driver.getDriver().get("http://aleph.bcucluj.ro:8991/");
                this.result = Result.OK;
                return this.result;
            } catch (Exception e) {
                trafficError();
            }
        }

    }

    private Result login() {
        while (true) {
            try {
//                login page
                driver.getDriver().navigate().to("http://aleph.bcucluj.ro:8991/");
                this.driver.getDriver().findElement(By.xpath("//a[contains(text(), 'Autentificare')]")).click();
//              notify -> on login page

////                workaround to not delete input automatically
                this.driver.getDriver().findElement(By.cssSelector("input[name = 'bor_id']")).click();
                this.driver.getDriver().findElement(By.cssSelector("input[name = 'bor_id']")).clear();
                Thread.sleep(100);

//                enter username and password
                Thread.sleep(500);
                this.driver.getDriver().findElement(By.cssSelector("input[name = 'bor_id']")).sendKeys(client.getId());
                Thread.sleep(100);
                this.driver.getDriver().findElement(By.cssSelector("input[name = 'bor_verification']")).sendKeys(client.getId());
                Thread.sleep(100);
//              notify -> entered username and password

//                click login button
                this.driver.getDriver().findElement(By.cssSelector("input[title='Autentificare utilizator cu permis valid']")).click();
//              notify -> logged in

                if (this.driver.getDriver().getPageSource().contains("ID-ul sau verificarea câmpului nu se potrivesc cu înregistrarea din sistem")) {
                    this.result = Result.INVALID_USER;
                    return this.result;
                }

                this.result = Result.OK;
                return this.result;
            } catch (Exception e) {
                trafficError();
                driver.getDriver().navigate().to("http://aleph.bcucluj.ro:8991/");
            }
        }
    }

    private Result seats() {
        while (true) {
            try {
                this.driver.getDriver().navigate().to("http://aleph.bcucluj.ro:8991");
                Thread.sleep(100);
                this.driver.getDriver().findElement(By.cssSelector("a[title='REZERVARE LOCURI - necesită AUTENTIFICARE']")).click();
//              notify -> selecting seats
                this.result = Result.OK;
                return this.result;
            } catch (Exception e) {
                trafficError();
            }
        }
    }

    private void trafficError() {
        while (this.driver.getDriver().getPageSource().contains("Incercati mai tarziu")) {
            this.driver.getDriver().navigate().refresh();
//          notify -> busy, retrying
        }
    }

    private Result book(WebElement seat) {
        String seatNumber = "";

        while (true) {
            try {
                seat.click();
//              notify -> selected seat

                String url = this.driver.getDriver().getCurrentUrl();
                Matcher seatNumberFind = Pattern.compile(".*item_sequence=000(\\d+)0.*").matcher(url);
                if (seatNumberFind.matches()) {
                    seatNumber = seatNumberFind.group(1);
                }

                this.driver.getDriver().findElement(By.cssSelector("input[alt=\"Order\"]")).click();
                this.driver.getDriver().findElement(By.cssSelector("input[src=\"http://aleph.bcucluj.ro:8991/exlibris/aleph/u23_1/alephe/www_f_rum/icon/fin-ru.gif\"]")).click();

                break;
            } catch (Exception e) {
                trafficError();
                seats();
            }
        }

        if (this.driver.getDriver().getPageSource().contains("Nu a fost selectat")) {
            getSeatList();
//          notify -> seat booked by someone else, retrying
        }

        if (this.driver.getDriver().getPageSource().contains("Cititorul a depăşit numărul de rezervări permise")) {
            this.result = Result.ALREADY_BOOKED;
            return this.result;
        }

        this.bookedSeat = seatNumber;
        this.result = Result.OK;
        return this.result;
    }

    private Result getSeatList() {
        while (true) {
            try {
                List<WebElement> freeSeats = this.driver.getDriver().findElements(By.cssSelector("a[href*=\"item_sequence=00\"]"));

                while (freeSeats.isEmpty()) {
                    this.driver.getDriver().navigate().refresh();
                    freeSeats.addAll(this.driver.getDriver().findElements(By.cssSelector("a[href*=\"item_sequence=00\"]")));
                }
//              notify -> found freeSeats.size() available seats

                boolean booked = false;
                for (WebElement seat : freeSeats) {
                    if (!booked) {
                        if (this.book(seat) == Result.OK) {
                            return Result.OK;
                        } else {
                            this.result = Result.ALREADY_BOOKED;
                            return this.result;
                        }
                    } else {
                        this.result = Result.OK;
                        return this.result;
                    }
                }

                break;
            } catch (Exception e) {
                trafficError();
                seats();
            }
        }

        return Result.ERROR;
    }
}
