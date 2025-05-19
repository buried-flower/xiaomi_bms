package org.example.mi_bms.service;

import org.example.mi_bms.entity.Rule;
import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.entity.WarnRequest;

import java.util.List;


public interface RuleService {
    List<WarnRequest> handleRules(String warnMessage)throws Exception;
}
