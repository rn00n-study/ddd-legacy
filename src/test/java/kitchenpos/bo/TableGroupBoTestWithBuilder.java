package kitchenpos.bo;

import kitchenpos.builder.OrderTableBuilder;
import kitchenpos.builder.TableGroupBuilder;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.model.OrderTable;
import kitchenpos.model.TableGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TableGroupBoTestWithBuilder extends MockTest{
    @Mock private OrderDao orderDao;
    @Mock private OrderTableDao orderTableDao;
    @Mock private TableGroupDao tableGroupDao;
    @InjectMocks private TableGroupBo tableGroupBo;

    @DisplayName("테이블그룹을 생성할 수 있다.")
    @Test
    void create() {
        //given
        OrderTable givenOrderTable1 = OrderTableBuilder.orderTable()
                .withNumberOfGuests(2)
                .withEmpty(true)
                .withTableGroupId(null)
                .withId(1L)
                .build();
        OrderTable givenOrderTable2 = OrderTableBuilder.orderTable()
                .withNumberOfGuests(2)
                .withEmpty(true)
                .withTableGroupId(null)
                .withId(2L)
                .build();

        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withCreatedDate(LocalDateTime.now())
                .withOrderTables(Arrays.asList(givenOrderTable1,givenOrderTable2))
                .withId(1L)
                .build();

        given(orderTableDao.findAllByIdIn(any(List.class)))
                .willReturn(Arrays.asList(givenOrderTable1,givenOrderTable2));
        given(tableGroupDao.save(any(TableGroup.class)))
                .willReturn(givenTableGroup);

        //when
        TableGroup actualTableGroup = tableGroupBo.create(givenTableGroup);

        //then
        assertThat(actualTableGroup.getId())
                .isEqualTo(givenTableGroup.getId());
    }

    @DisplayName("주문이 없는 테이블은 테이블그룹으로 생성할 수 없다.")
    @ParameterizedTest
    @NullSource
    void createWithoutOrder(List<OrderTable> orderTableList) {
        //given
        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withOrderTables(orderTableList)
                .build();

        //when
        //then
        assertThatThrownBy(() -> {
            tableGroupBo.create(givenTableGroup); })
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문이 있는 경우에도 두개 이상의 테이블만 테이블그룹으로 생성할 수 있다.")
    @Test
    void createOverTwoTables() {
        //given
        OrderTable givenOrderTable = OrderTableBuilder.orderTable()
                .build();
        List<OrderTable> givenOrderTables = Arrays.asList(givenOrderTable);

        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withOrderTables(givenOrderTables)
                .build();

        //when
        //then
        assertThatThrownBy(() -> {
            tableGroupBo.create(givenTableGroup); })
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("주문이 있는 테이블수와 생성하려는 테이블수가 같아야 한다.")
    @Test
    void createNumberOfTable() {
        //given
        OrderTable givenOrderTable1 = OrderTableBuilder.orderTable()
                .withId(1L)
                .build();
        OrderTable givenOrderTable2 = OrderTableBuilder.orderTable()
                .withId(2L)
                .build();
        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withOrderTables(Arrays.asList(givenOrderTable1,givenOrderTable2))
                .withId(1L)
                .build();

        //when
        given(orderTableDao.findAllByIdIn(any(List.class)))
                .willReturn(Collections.singletonList(givenOrderTable1));
        //then
        assertThatThrownBy(() -> {
            tableGroupBo.create(givenTableGroup); })
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블그룹을 생성하는 대상 테이블은 비어있어야 한다.")
    @ParameterizedTest
    @ValueSource(booleans = {false})
    void createEmptyTable(boolean isEmpty) {
        //given
        OrderTable givenOrderTable1 = OrderTableBuilder.orderTable()
                .withEmpty(isEmpty)
                .withId(1L)
                .build();
        OrderTable givenOrderTable2 = OrderTableBuilder.orderTable()
                .withEmpty(isEmpty)
                .withId(2L)
                .build();
        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withOrderTables(Arrays.asList(givenOrderTable1,givenOrderTable2))
                .withId(1L)
                .build();

        //when
        given(orderTableDao.findAllByIdIn(any(List.class)))
                .willReturn(Arrays.asList(givenOrderTable1,givenOrderTable2));
        //then
        assertThatThrownBy(() -> {
            tableGroupBo.create(givenTableGroup); })
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테이블그룹을 삭제할 수 있다.")
    @ParameterizedTest
    @MethodSource("booleanAndIntProvider")
    void delete(boolean isExists, int numberOfDeletedTables) {
        //given
        OrderTable givenOrderTable1 = OrderTableBuilder.orderTable()
                .withId(1L)
                .build();
        OrderTable givenOrderTable2 = OrderTableBuilder.orderTable()
                .withId(2L)
                .build();
        List<OrderTable> givenOrderTableList = Arrays.asList(givenOrderTable1,givenOrderTable2);
        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withId(1L)
                .build();

        given(orderTableDao.findAllByTableGroupId(anyLong()))
                .willReturn(givenOrderTableList);
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(any(),any()))
                .willReturn(isExists);

        //when
        tableGroupBo.delete(givenTableGroup.getId());

        //then
        verify(orderTableDao,times(numberOfDeletedTables))
                .save(any(OrderTable.class));
    }

    static Stream<Arguments> booleanAndIntProvider() {
        return Stream.of(Arguments.arguments(false, 2));
    }

    @DisplayName("테이블상태가 조리중: COOKING, 고객이 식사중인 주문: MEAL 인 경우는 삭제할 수 없다.")
    @ParameterizedTest
    @ValueSource(booleans = {true})
    void deleteByStatus(boolean isExists) {
        //given
        OrderTable givenOrderTable1 = OrderTableBuilder.orderTable()
                .withId(1L)
                .build();
        OrderTable givenOrderTable2 = OrderTableBuilder.orderTable()
                .withId(2L)
                .build();
        List<OrderTable> givenOrderTableList = Arrays.asList(givenOrderTable1,givenOrderTable2);
        TableGroup givenTableGroup = TableGroupBuilder.tableGroup()
                .withId(1L)
                .build();

        given(orderTableDao.findAllByTableGroupId(anyLong()))
                .willReturn(givenOrderTableList);
        given(orderDao.existsByOrderTableIdInAndOrderStatusIn(any(),any()))
                .willReturn(isExists);

        //when
        //then
        assertThatThrownBy(() -> {
            tableGroupBo.delete(givenTableGroup.getId()); })
                .isInstanceOf(IllegalArgumentException.class);
    }
}