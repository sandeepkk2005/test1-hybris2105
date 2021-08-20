/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.customerticketingocc.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.commerceservices.storefinder.data.StoreFinderSearchPageData;
import de.hybris.platform.customerticketingfacades.TicketFacade;
import de.hybris.platform.customerticketingfacades.data.StatusData;
import de.hybris.platform.customerticketingfacades.data.TicketAssociatedData;
import de.hybris.platform.customerticketingfacades.data.TicketCategory;
import de.hybris.platform.customerticketingfacades.data.TicketData;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketAssociatedObjectListWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketAssociatedObjectWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketCategoryListWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketCategoryWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketEventWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketListWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketStarterWsDTO;
import de.hybris.platform.customerticketingocc.dto.ticket.TicketWsDTO;
import de.hybris.platform.customerticketingocc.errors.exceptions.TicketCreateException;
import de.hybris.platform.customerticketingocc.errors.exceptions.TicketEventCreateException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.webservicescommons.errors.exceptions.NotFoundException;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class TicketsControllerTest
{
	private static final String FIELDS = "MY_FIELDS";
	private static final int CURRENT_PAGE = 101;
	private static final int PAGE_SIZE = 555;
	private static final String SORT = "MY_SORT";
	private static final String TICKET_CATEGORY_ENQUIRY = "ENQUIRY";
	private static final String TICKET_ID = "MY_TICKET_ID";
	private static final String TICKET_MESSAGE = "MY_TICKET_MESSAGE";
	private static final String TICKET_STATUS_CLOSED = "CLOSED";
	private static final String TICKET_STATUS_OPEN = "OPEN";
	private static final String TICKET_SUBJECT = "MY_TICKET_SUBJECT";
	private static final String INVALID_STATUS = "invalidStatus";

	@Mock
	private TicketFacade ticketFacade;
	@Mock
	private DataMapper dataMapper;
	@Mock
	private Validator ticketStarterValidator;
	@Mock
	private Validator ticketEventValidator;
	@Captor
	private ArgumentCaptor<PageableData> pageableDataCaptor;
	@InjectMocks
	private TicketsController ticketsController;

	@Test
	public void testGetTickets()
	{
		final SearchPageData<TicketData> ticketSearchPageData = new StoreFinderSearchPageData<>();
		final TicketListWsDTO ticketListWsDTO = new TicketListWsDTO();

		when(ticketFacade.getTickets(any())).thenReturn(ticketSearchPageData);
		when(dataMapper.map(any(), eq(TicketListWsDTO.class), eq(FIELDS))).thenReturn(ticketListWsDTO);
		final TicketListWsDTO result = ticketsController.getTickets(CURRENT_PAGE, PAGE_SIZE, SORT, FIELDS);

		verify(ticketFacade).getTickets(pageableDataCaptor.capture());
		verify(dataMapper).map(any(), eq(TicketListWsDTO.class), eq(FIELDS));

		assertThat(result).isSameAs(ticketListWsDTO);
		assertThat(pageableDataCaptor.getValue()).isNotNull()
				.hasFieldOrPropertyWithValue("currentPage", CURRENT_PAGE)
				.hasFieldOrPropertyWithValue("pageSize", PAGE_SIZE)
				.hasFieldOrPropertyWithValue("sort", SORT);
	}

	@Test
	public void testGetTicket()
	{
		final TicketData ticketData = new TicketData();
		final TicketWsDTO ticketWsDTO = new TicketWsDTO();
		ticketData.setSubject(TICKET_SUBJECT);
		ticketData.setId(TICKET_ID);

		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(ticketData);
		when(dataMapper.map(ticketData, TicketWsDTO.class, FIELDS)).thenReturn(ticketWsDTO);

		final TicketWsDTO response = ticketsController.getTicket(TICKET_ID, FIELDS);

		verify(ticketFacade).getTicket(TICKET_ID);
		verify(dataMapper).map(ticketData, TicketWsDTO.class, FIELDS);
		assertThat(response).isNotNull()
				.isSameAs(ticketWsDTO);
	}

	@Test
	public void testGetTicketNotFound()
	{
		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(null);

		assertThatThrownBy(() -> ticketsController.getTicket(TICKET_ID, FIELDS))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("Ticket not found for the given ID "+TICKET_ID);
		verifyNoMoreInteractions(dataMapper);
	}

	@Test
	public void testGetTicketNotFound_withException()
	{
		when(ticketFacade.getTicket(TICKET_ID)).thenThrow(new RuntimeException("=== Exception ==="));

		assertThatThrownBy(() -> ticketsController.getTicket(TICKET_ID, FIELDS))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("Ticket not found for the given ID "+TICKET_ID);
		verifyNoMoreInteractions(dataMapper);
	}

	@Test
	public void testCreateTicket()
	{
		final TicketData ticketData = new TicketData();
		final TicketData createdTicketData = new TicketData();
		createdTicketData.setId(TICKET_ID);
		final TicketData returnedTicketData = new TicketData();

		final TicketWsDTO ticketWsDTO = new TicketWsDTO();
		final TicketCategoryWsDTO ticketCategoryWsDTO = new TicketCategoryWsDTO();
		ticketCategoryWsDTO.setId(TICKET_CATEGORY_ENQUIRY);
		final TicketStarterWsDTO ticketStarter = new TicketStarterWsDTO();
		ticketStarter.setSubject(TICKET_SUBJECT);
		ticketStarter.setMessage(TICKET_MESSAGE);
		ticketStarter.setTicketCategory(ticketCategoryWsDTO);

		when(ticketFacade.createTicket(any())).thenReturn(createdTicketData);
		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(returnedTicketData);
		when(dataMapper.map(eq(returnedTicketData), eq(TicketWsDTO.class), eq(FIELDS))).thenReturn(ticketWsDTO);
		when(dataMapper.map(eq(ticketStarter), eq(TicketData.class))).thenReturn(ticketData);

		TicketWsDTO result = ticketsController.createTicket(ticketStarter, FIELDS);

		verify(ticketFacade).createTicket(ticketData);
		verify(ticketFacade).getTicket(TICKET_ID);
		verify(dataMapper).map(any(), eq(TicketData.class));
		verify(dataMapper).map(any(), eq(TicketWsDTO.class), eq(FIELDS));

		assertThat(result).isSameAs(ticketWsDTO);
	}


	@Test
	public void testCreateTicketFailWithRunTimeError()
	{
		final TicketStarterWsDTO ticketStarter = new TicketStarterWsDTO();
		doThrow(new RuntimeException("Exception message")).when(ticketFacade).createTicket(any());
		assertThatThrownBy(() -> ticketsController.createTicket(ticketStarter, FIELDS))
				.isInstanceOf(TicketCreateException.class)
				.hasMessage("Encountered an error when creating a new ticket");
	}

	@Test
	public void testCreateTicketFailWithUnknownIdentifierError()
	{
		final TicketStarterWsDTO ticketStarter = new TicketStarterWsDTO();
		final String errMsg = "Ticket create failed";
		doThrow(new UnknownIdentifierException(errMsg)).when(ticketFacade).createTicket(any());
		assertThatThrownBy(() -> ticketsController.createTicket(ticketStarter, FIELDS))
				.isInstanceOf(TicketCreateException.class)
				.hasMessage(errMsg);
	}

	@Test
	public void testCreateTicketFailWithValidationError()
	{
		final TicketStarterWsDTO ticketStarter = new TicketStarterWsDTO();
		final String errMsg = "Ticket create failed for \"headline\" is bigger than 255";
		doThrow(new ModelSavingException(null, new InterceptorException(errMsg))).when(ticketFacade).createTicket(any());
		assertThatThrownBy(() -> ticketsController.createTicket(ticketStarter, FIELDS))
				.isInstanceOf(TicketCreateException.class)
				.hasMessage(errMsg.replace("headline", "Subject"));
	}

	@Test
	public void testCreateTicketWhenValidationFailed()
	{
		final TicketStarterWsDTO ticketStarter = new TicketStarterWsDTO();
		doAnswer(invocationOnMock -> {
			final Errors errors = invocationOnMock.getArgumentAt(1, Errors.class);
			errors.rejectValue("subject", "field.required");
			return null;
		}).when(ticketStarterValidator).validate(eq(ticketStarter), any());
		assertThatThrownBy(() -> ticketsController.createTicket(ticketStarter, FIELDS))
				.isInstanceOf(WebserviceValidationException.class);
	}

	@Test
	public void testCreateTicketEventWithStatus()
	{
		final TicketData ticketData = new TicketData();
		ticketData.setMessage(TICKET_MESSAGE);
		final StatusData statusData = new StatusData();
		statusData.setId(TICKET_STATUS_CLOSED);
		ticketData.setStatus(statusData);

		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		ticketEvent.setMessage(TICKET_MESSAGE);
		final TicketData existingTicketData = new TicketData();
		existingTicketData.setId(TICKET_ID);
		existingTicketData.setStatus(statusData);
		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(existingTicketData);
		existingTicketData.setAvailableStatusTransitions(List.of(statusData));


		when(dataMapper.map(eq(ticketEvent), eq(TicketData.class))).thenReturn(ticketData);

		ticketsController.createTicketEvent(TICKET_ID, ticketEvent);

		verify(ticketFacade).updateTicket(ticketData);
	}

	@Test
	public void testCreateTicketEventWithoutStatus()
	{
		final TicketData ticketData = new TicketData();
		ticketData.setMessage(TICKET_MESSAGE);

		final TicketData existingTicketData = new TicketData();
		existingTicketData.setId(TICKET_ID);
		final StatusData statusData = new StatusData();
		statusData.setId(TICKET_STATUS_OPEN);
		existingTicketData.setStatus(statusData);

		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		ticketEvent.setMessage(TICKET_MESSAGE);

		when(dataMapper.map(eq(ticketEvent), eq(TicketData.class))).thenReturn(ticketData);
		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(existingTicketData);

		ticketsController.createTicketEvent(TICKET_ID, ticketEvent);

		ticketData.setStatus(statusData);
		verify(ticketFacade).updateTicket(ticketData);
		verify(ticketFacade).getTicket(TICKET_ID);
	}

	@Test
	public void testCreateTicketEventWithoutStatusAndTicketDoesNotExist()
	{
		final TicketData ticketData = new TicketData();
		ticketData.setMessage(TICKET_MESSAGE);

		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		ticketEvent.setMessage(TICKET_MESSAGE);

		when(dataMapper.map(eq(ticketEvent), eq(TicketData.class))).thenReturn(ticketData);
		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(null);

		assertThatThrownBy(() -> ticketsController.createTicketEvent(TICKET_ID, ticketEvent))
				.isInstanceOf(TicketEventCreateException.class)
				.hasMessage("Ticket not found for the given ID " + TICKET_ID);;
	}

	@Test
	public void testCreateTicketEventWhenUpdateTicketFailed()
	{
		final TicketData ticketData = new TicketData();
		ticketData.setMessage(TICKET_MESSAGE);
		final StatusData statusData = new StatusData();
		statusData.setId(TICKET_STATUS_OPEN);
		ticketData.setStatus(statusData);

		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		ticketEvent.setMessage(TICKET_MESSAGE);

		final TicketData existingTicketData = new TicketData();
		existingTicketData.setId(TICKET_ID);
		existingTicketData.setStatus(statusData);
		existingTicketData.setAvailableStatusTransitions(List.of(statusData));
		final String errMsg = "Unable to add ticketEvent to the ticket with given ID " + TICKET_ID;

		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(existingTicketData);

		when(dataMapper.map(eq(ticketEvent), eq(TicketData.class))).thenReturn(ticketData);
		doAnswer(invocationOnMock ->
		{
			throw new RuntimeException(errMsg);
		}).when(ticketFacade).updateTicket(ticketData);

		assertThatThrownBy(() -> ticketsController.createTicketEvent(TICKET_ID, ticketEvent))
				.isInstanceOf(TicketEventCreateException.class)
				.hasMessage(errMsg);
	}

	@Test
	public void testCreateTicketEventWhenNewStateIsNotAllowed()
	{
		final TicketData ticketData = new TicketData();
		ticketData.setMessage(TICKET_MESSAGE);
		final StatusData toStatus = new StatusData();
		toStatus.setId(INVALID_STATUS);
		ticketData.setStatus(toStatus);

		final StatusData statusData = new StatusData();
		statusData.setId(TICKET_STATUS_OPEN);
		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		ticketEvent.setMessage(TICKET_MESSAGE);

		final TicketData existingTicketData = new TicketData();
		existingTicketData.setId(TICKET_ID);
		existingTicketData.setStatus(statusData);
		existingTicketData.setAvailableStatusTransitions(List.of(statusData));

		when(ticketFacade.getTicket(TICKET_ID)).thenReturn(existingTicketData);

		when(dataMapper.map(eq(ticketEvent), eq(TicketData.class))).thenReturn(ticketData);

		assertThatThrownBy(() -> ticketsController.createTicketEvent(TICKET_ID, ticketEvent))
				.isInstanceOf(TicketEventCreateException.class)
				.hasMessage("Unable to change ticket status to " + INVALID_STATUS + " for the ticket " + TICKET_ID);
	}

	@Test
	public void testCreateTicketEventWhenValidationFailed()
	{
		final TicketEventWsDTO ticketEvent = new TicketEventWsDTO();
		doAnswer(invocationOnMock ->
		{
			final Errors errors = invocationOnMock.getArgumentAt(1, Errors.class);
			errors.rejectValue("message", "field.required");
			return null;
		}).when(ticketEventValidator).validate(eq(ticketEvent), any());
		assertThatThrownBy(() -> ticketsController.createTicketEvent(TICKET_ID, ticketEvent))
				.isInstanceOf(WebserviceValidationException.class);
	}

	@Test
	public void testGetTicketCategories()
	{
		final List<TicketCategory> ticketCategories = new ArrayList<TicketCategory>();
		final List<TicketCategoryWsDTO> ticketCategoriesList = new ArrayList<TicketCategoryWsDTO>();

		when(ticketFacade.getTicketCategories()).thenReturn(ticketCategories);
		when(dataMapper.mapAsList(any(), eq(TicketCategoryWsDTO.class), any())).thenReturn(ticketCategoriesList);

		final TicketCategoryListWsDTO result = ticketsController.getTicketCategories();

		verify(ticketFacade).getTicketCategories();
		verify(dataMapper).mapAsList(any(), eq(TicketCategoryWsDTO.class), any());

		assertThat(result.getTicketCategories()).isSameAs(ticketCategoriesList);
	}

	@Test
	public void testGetTicketAssociatedObjects()
	{
		final Map<String, List<TicketAssociatedData>> associatedObjectDataMap = new HashMap<>();
		final List<TicketAssociatedObjectWsDTO> ticketAssociatedObjects = new ArrayList<>();

		when(ticketFacade.getAssociatedToObjects()).thenReturn(associatedObjectDataMap);
		when(dataMapper.mapAsList(any(), eq(TicketAssociatedObjectWsDTO.class), any())).thenReturn(ticketAssociatedObjects);

		final TicketAssociatedObjectListWsDTO result = ticketsController.getTicketAssociatedObjects();

		verify(ticketFacade).getAssociatedToObjects();
		verify(dataMapper).mapAsList(any(), eq(TicketAssociatedObjectWsDTO.class), any());

		assertThat(result.getTicketAssociatedObjects()).isSameAs(ticketAssociatedObjects);
	}
}
