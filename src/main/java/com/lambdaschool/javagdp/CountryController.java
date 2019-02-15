package com.lambdaschool.javagdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class CountryController {

    private final CountryRepository countryrepo;
    private final RabbitTemplate rt;

    public CountryController(CountryRepository countryrepo, RabbitTemplate rt)
    {
        this.countryrepo = countryrepo;
        this.rt = rt;
    }

    @GetMapping("/names")
    public List<Country> names()
    {
        List<Country> countryList = countryrepo.findAll();
        countryList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        return countryList;
    }

    @GetMapping("/economy")
    public List<Country> economy()
    {
        List<Country> countryList = countryrepo.findAll();
        countryList.sort((c1, c2) -> (int)(c2.getGdp() - c1.getGdp()));
        return countryList;
    }

    @GetMapping("/total")
    public ObjectNode total()
    {
        List<Country> countryList = countryrepo.findAll();
        Long total = 0L;
        for (Country c : countryList)
        {
            total += c.getGdp();
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode totalGDP = mapper.createObjectNode();
        totalGDP.put("id", 0);
        totalGDP.put("country", "total");
        totalGDP.put("gdp", total);

        return totalGDP;
    }

    @GetMapping("/gdp/{name}")
    public Country findCountry(@PathVariable String name)
    {
        List<Country> countryList = countryrepo.findAll();
        Country country = new Country("Not found", 0L);
        for (Country c : countryList)
        {
            if (c.getName().equals(name))
            {
                country = c;
            }
        }
        CountryLog message = new CountryLog("Checked countries by name");
        rt.convertAndSend(JavaGdpApplication.QUEUE_NAME, message.toString());
        log.info("Message sent");
        return country;
    }

    @PostMapping("/gdp")
    public List<Country> gdp(@RequestBody List<Country> newCountries)
    {
        return countryrepo.saveAll(newCountries);
    }
}
