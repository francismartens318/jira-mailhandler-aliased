/*

Copyright (c) 2012, Mike Eldridge <diz@tripside.org>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

  - Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

  - Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

***********************************************************************/

package org.tripside.jira.plugin.mailhandler.aliased;

import com.atlassian.jira.plugins.mail.webwork.AbstractEditHandlerDetailsWebAction;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.collect.MapBuilder;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.plugin.PluginAccessor;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class EditAliasedMessageHandlerDetailsWebAction extends AbstractEditHandlerDetailsWebAction
{
	static final Logger log = Logger.getLogger(EditAliasedMessageHandlerDetailsWebAction.class);

	private String handledEmailAddress;
	private Boolean restrictRecipient = Boolean.FALSE;

	public EditAliasedMessageHandlerDetailsWebAction (PluginAccessor pluginAccessor)
	{
		super(pluginAccessor);
	}

	public String getPattern ()
	{
		return handledEmailAddress;
	}

	public void setPattern (String pattern)
	{
		handledEmailAddress = pattern;
	}

	public String getRestrictRecipient ()
	{
		return restrictRecipient.toString();
	}

	public void setRestrictRecipient (String value)
	{
		restrictRecipient = new Boolean (value);
	}

	@Override
	protected void copyServiceSettings (JiraServiceContainer jiraServiceContainer) throws ObjectConfigurationException
	{
		final Map <String, String> params = ServiceUtils.getParameterMap(jiraServiceContainer.getProperty(AbstractMessageHandlingService.KEY_HANDLER_PARAMS));

		handledEmailAddress = params.get("pattern");
		restrictRecipient = new Boolean (params.get("restrict-recipient"));
	}

	@Override
	protected Map <String, String> getHandlerParams ()
	{
		log.warn("getHandlerParams");
		log.warn("getHandlerParams: restrictRecipient=" + (restrictRecipient ? "true" : "false"));
		return ImmutableMap.<String, String>builder()
			.put("pattern",				handledEmailAddress)
			.put("restrict-recipient",	restrictRecipient.toString())
			.build();
	}

	@Override
	protected void doValidation ()
	{
		if (configuration == null)
			return;

		super.doValidation();
	}
}
