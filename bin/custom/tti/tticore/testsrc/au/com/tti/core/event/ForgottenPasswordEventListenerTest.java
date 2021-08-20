package au.com.tti.core.event;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.ForgottenPwdEvent;
import de.hybris.platform.commerceservices.model.process.ForgottenPasswordProcessModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class ForgottenPasswordEventListenerTest
{
	@Mock
	protected ModelService modelService;

	@Mock
	private BusinessProcessService businessProcessService;

	@Mock
	private ForgottenPwdEvent event;

	@Mock
	private ForgottenPasswordProcessModel forgottenPasswordProcessModel;

	@Mock
	private CustomerModel customerModel;

	@Mock
	private BaseSiteModel site;

	@Spy
	@InjectMocks
	private ForgottenPasswordEventListener listener;

	@Before
	public void setup()
	{
		//action.setQuoteNotificationType(QuoteNotificationType.EXPIRING_SOON);
	}

	@Test
	public void testOnSiteEvent() throws Exception
	{
		given(event.getCustomer()).willReturn(customerModel);
		given(customerModel.getUid()).willReturn("customer-1");
		given(businessProcessService.createProcess(any(), any())).willReturn(forgottenPasswordProcessModel);
		listener.onSiteEvent(event);
		verify(businessProcessService).createProcess(any(), any());
		verify(modelService).save(forgottenPasswordProcessModel);
		verify(businessProcessService).startProcess(forgottenPasswordProcessModel);
	}

	@Test
	public void testGetSiteChannelForEvent() throws Exception
	{
		final SiteChannel channel = SiteChannel.B2B;
		given(event.getSite()).willReturn(site);
		given(site.getChannel()).willReturn(channel);
		assertThat(listener.getSiteChannelForEvent(event)).isEqualTo(channel);
	}

	@Test
	public void testTestThis() throws Exception
	{
		assertThat(listener.testThis("name")).isEqualTo("name-Test-This");
	}
}
