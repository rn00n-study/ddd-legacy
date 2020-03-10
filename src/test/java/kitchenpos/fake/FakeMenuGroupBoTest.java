package kitchenpos.fake;

import kitchenpos.bo.MenuGroupBo;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.model.MenuGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FakeMenuGroupBoTest {
    private static final long MENU_GROUP_ID_ONE = 1L;
    private static final long MENU_GROUP_ID_TWO = 2L;

    private MenuGroupDao menuGroupDao = new FakeMenuGroupDao();

    private MenuGroupBo menuGroupBo;

    @BeforeEach
    void setUp() {
        this.menuGroupBo = new MenuGroupBo(menuGroupDao);
    }

    @DisplayName("메뉴 그룹 조회")
    @Test
    void list() {
        MenuGroup menugroup1 = createMenuGroup(MENU_GROUP_ID_ONE, "순살 치킨");
        MenuGroup menugroup2 = createMenuGroup(MENU_GROUP_ID_TWO, "오븐 치킨");

        menuGroupDao.save(menugroup1);
        menuGroupDao.save(menugroup2);

        List<MenuGroup> menuGroups = Arrays.asList(menugroup1, menugroup2);

        List<MenuGroup> findList = menuGroupBo.list();

        assertAll(
                () -> assertThat(findList).containsAll(menuGroups),
                () -> assertThat(findList.size()).isEqualTo(menuGroups.size())
        );
    }

    @DisplayName("메뉴 그룹 추가")
    @Test
    void create() {
        MenuGroup menugroup = createMenuGroup(MENU_GROUP_ID_ONE, "순살 치킨");
        MenuGroup savedMenuGroup = menuGroupBo.create(menugroup);

        assertAll(
                () -> assertThat(savedMenuGroup.getId()).isEqualTo(menugroup.getId()),
                () -> assertThat(savedMenuGroup.getName()).isEqualTo(menugroup.getName())
        );
    }

    private MenuGroup createMenuGroup(Long menuGroupId, String menuGroupName) {
        MenuGroup menuGroup = new MenuGroup();

        menuGroup.setId(menuGroupId);
        menuGroup.setName(menuGroupName);

        return menuGroup;
    }
}