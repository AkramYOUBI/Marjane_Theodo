package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository pr;

    private final NotificationService ns;

    public ProductService(ProductRepository pr, NotificationService ns) {
        this.pr = pr;
        this.ns = ns;
    }

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
        /*if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            ns.sendOutOfStockNotification(p.getName());
            pr.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }*/

        LocalDate today = LocalDate.now();
        LocalDate seasonEnd = p.getSeasonEndDate();
        LocalDate seasonStart = p.getSeasonStartDate();
        int leadTime = p.getLeadTime();

        if (today.plusDays(leadTime).isAfter(seasonEnd) || seasonStart.isAfter(today)) {
            ns.sendOutOfStockNotification(p.getName());
            if (p.getAvailable() > 0) {
                p.setAvailable(0);
                pr.save(p);
            }
        } else {
            notifyDelay(leadTime, p);
        }
    }

    public void handleExpiredProduct(Product p) {
        /*if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            pr.save(p);
        }*/
        LocalDate today = LocalDate.now();

        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(today)) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            if (p.getAvailable() > 0) {
                p.setAvailable(0);
                pr.save(p);
            }
        }
    }

}