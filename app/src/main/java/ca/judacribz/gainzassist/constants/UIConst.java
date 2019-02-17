package ca.judacribz.gainzassist.constants;

import ca.judacribz.gainzassist.adapters.SingleItemAdapter;

import java.util.HashMap;
import java.util.Map;

import static ca.judacribz.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS.*;

public class UIConst {
    public static final int PROGRESS_MAX = 100;
    public static final Map<SingleItemAdapter.PROGRESS_STATUS, Integer> PROGRESS_CODE_MAP
            = new HashMap<SingleItemAdapter.PROGRESS_STATUS, Integer>() {{
                put(UNSELECTED, 0);
                put(SELECTED, 1);
                put(SUCCESS, 2);
                put(FAIL, 3);
                put(SUCCESS_SELECTED, 4);
                put(FAIL_SELECTED, 5);
    }};

    public static final Map<Integer, SingleItemAdapter.PROGRESS_STATUS> PROGRESS_STATUS_MAP
            = new HashMap<Integer, SingleItemAdapter.PROGRESS_STATUS>() {{
        put(0, UNSELECTED);
        put(1, SELECTED);
        put(2, SUCCESS);
        put(3, FAIL);
        put(4, SUCCESS_SELECTED);
        put(5, FAIL_SELECTED);
    }};
}
