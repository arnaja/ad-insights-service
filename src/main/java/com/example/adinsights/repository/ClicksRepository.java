package com.example.adinsights.repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;

@Repository
@RequiredArgsConstructor
public class ClicksRepository {

    private final CqlSession session;

    private PreparedStatement preparedStatement;

    /**
     * Initialize prepared statement once
     */
    @PostConstruct
    public void init() {

        Select select = selectFrom("campaign_clicks")
                .column("clicks")
                .whereColumn("tenant_id").isEqualTo(bindMarker())
                .whereColumn("campaign_id").isEqualTo(bindMarker());

        preparedStatement = session.prepare(select.build());
    }

    /**
     * Fetch clicks using prepared + bound statement
     */
    public int getClicks(String campaignId, String tenantId) {

        BoundStatement bound = preparedStatement.bind(tenantId, campaignId);

        ResultSet rs = session.execute(bound);
        Row row = rs.one();

        return (row != null) ? row.getInt("clicks") : 0;
    }
}
