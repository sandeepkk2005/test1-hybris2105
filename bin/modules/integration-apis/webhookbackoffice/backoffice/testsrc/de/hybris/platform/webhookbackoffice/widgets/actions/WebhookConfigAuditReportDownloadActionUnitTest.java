package de.hybris.platform.webhookbackoffice.widgets.actions;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.testing.AbstractActionUnitTest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.integrationbackoffice.widgets.common.utility.EditorAccessRights;
import de.hybris.platform.integrationbackoffice.widgets.modals.builders.AuditReportBuilder;
import de.hybris.platform.webhookservices.model.WebhookConfigurationModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
public class WebhookConfigAuditReportDownloadActionUnitTest extends AbstractActionUnitTest<WebhookConfigAuditReportDownloadAction>
{
    private static final WebhookConfigurationModel DATA = new WebhookConfigurationModel();

    private static final Object BAD_DATA = new Object();

    @Mock
    private AuditReportBuilder reportBuilder;

    @Mock
    private ActionContext<Object> ctx;

    @Mock
    private EditorAccessRights editorAccessRights;

    @InjectMocks
    private WebhookConfigAuditReportDownloadAction action = new WebhookConfigAuditReportDownloadAction();

    @Override
    public WebhookConfigAuditReportDownloadAction getActionInstance()
    {
        return action;
    }

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCannotPerformWithNull()
    {
        when(ctx.getData()).thenReturn(null);

        assertThat(action.canPerform(ctx)).isFalse();
    }

    @Test
    public void testCannotPerformWithWrongDataType()
    {
        when(ctx.getData()).thenReturn(BAD_DATA);

        assertThat(action.canPerform(ctx)).isFalse();
    }

    @Test
    public void testCannotPerformIfNotAdmin()
    {
        when(ctx.getData()).thenReturn(DATA);
        when(editorAccessRights.isUserAdmin()).thenReturn(false);

        assertThat(action.canPerform(ctx)).isFalse();
    }

    @Test
    public void testCanPerformWithWebhookConfigurationModel()
    {
        when(ctx.getData()).thenReturn(DATA);
        when(editorAccessRights.isUserAdmin()).thenReturn(true);

        assertThat(action.canPerform(ctx)).isTrue();
    }

    @Test
    public void testPerformErrorWithWrongDataType()
    {
        when(ctx.getData()).thenReturn(BAD_DATA);
        when(editorAccessRights.isUserAdmin()).thenReturn(true);

        assertThat(action.perform(ctx).getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void testPerformErrorWithNull()
    {
        when(editorAccessRights.isUserAdmin()).thenReturn(true);

        assertThat(action.perform(null).getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void testPerformErrorWithRuntimeException()
    {
        when(ctx.getData()).thenReturn(DATA);
        when(editorAccessRights.isUserAdmin()).thenReturn(true);
        when(reportBuilder.generateAuditReport(DATA)).thenThrow(new RuntimeException());

        assertThat(action.perform(ctx).getResultCode()).isEqualTo(ActionResult.ERROR);
    }

    @Test
    public void testPerformSuccess()
    {
        when(ctx.getData()).thenReturn(DATA);
        when(editorAccessRights.isUserAdmin()).thenReturn(true);

        assertThat(action.perform(ctx).getResultCode()).isEqualTo(ActionResult.SUCCESS);
    }
}
