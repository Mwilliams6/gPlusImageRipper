package com.revolv3r.gplusimageripper;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/")
public class MainController {

  @RequestMapping
  public ModelAndView get() {
    return new ModelAndView("index");
  }
}