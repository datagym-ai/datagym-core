package ai.datagym.application.testUtils;

import com.eforce21.lib.bin.file.entity.BinFileEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BinfileEntityUtils {
    public static final long BIN_FILE_ENTITY_ID = 1;
    public static final long BIN_FILE_ENTITY_SIZE = 1;
    public static final String BIN_FILE_ENTITY_NAME = "BinFileEntityName " + UUID.randomUUID();
    public static final String BIN_FILE_ENTITY_DATA_ID = "DataId " + UUID.randomUUID();

    private static final Long TIME = System.currentTimeMillis();

    public static BinFileEntity createTestBinFileEntity() {
        return new BinFileEntity() {{
            setId(BIN_FILE_ENTITY_ID);
            setName(BIN_FILE_ENTITY_NAME);
            setMime("image/jpeg");
            setCover(false);
            setDataId(BIN_FILE_ENTITY_DATA_ID);
            setSize(BIN_FILE_ENTITY_SIZE);
            setTsCreate(TIME);
        }};
    }

    public static List<BinFileEntity> createTestBinFileEntityList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new BinFileEntity() {{
                    setId(BIN_FILE_ENTITY_ID + index);
                    setName(BIN_FILE_ENTITY_NAME + index);
                    setMime("image/jpeg");
                    setCover(false);
                    setDataId(BIN_FILE_ENTITY_DATA_ID + index);
                    setSize(BIN_FILE_ENTITY_SIZE + index);
                    setTsCreate(TIME);
                }})
                .collect(Collectors.toList());
    }
}
