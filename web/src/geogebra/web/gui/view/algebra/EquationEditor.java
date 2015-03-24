package geogebra.web.gui.view.algebra;

import geogebra.common.kernel.Macro;
import geogebra.common.main.App;
import geogebra.common.main.Localization;
import geogebra.common.util.AutoCompleteDictionary;
import geogebra.common.util.StringUtil;
import geogebra.html5.gui.view.autocompletion.CompletionsPopup;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class EquationEditor {
	protected SuggestBox.SuggestionCallback sugCallback = new SuggestBox.SuggestionCallback() {
		public void onSuggestionSelected(Suggestion s) {

			String sugg = s.getReplacementString();
			// For now, we can assume that sugg is in LaTeX format,
			// and if it will be wrong, we can revise it later
			// at the moment we shall focus on replacing the current
			// word in MathQuillGGB with it...

			// Although MathQuillGGB could compute the current word,
			// it might not be the same as the following, as
			// maybe it can be done easily for English characters
			// but current word shall be internationalized to e.g.
			// Hungarian, or even Arabic, Korean, etc. which are
			// known by GeoGebra but unknown by MathQuillGGB...
			updateCurrentWord(false);
			String currentWord = curWord.toString();

			// So we also provide currentWord as a heuristic or helper:
			geogebra.html5.main.DrawEquationWeb.writeLatexInPlaceOfCurrentWord(
			        seMayLatex, sugg, currentWord, true);

			// not to forget making the popup disappear after success!
			sug.hideSuggestions();
		}
	};

	protected SuggestOracle.Callback popupCallback = new SuggestOracle.Callback() {
		public void onSuggestionsReady(SuggestOracle.Request req,
		        SuggestOracle.Response res) {
			component.updatePosition(sug);
			sug.accessShowSuggestions(res, popup, sugCallback);
		}
	};
	protected AutoCompleteDictionary dict;
	protected ScrollableSuggestionDisplay sug;
	public static int querylimit = 5000;
	private List<String> completions;
	StringBuilder curWord;
	private int curWordStart;
	protected CompletionsPopup popup;
	private App app;

	private EquationEditorListener component;
	private SpanElement seMayLatex;

	public EquationEditor(App app, EquationEditorListener component) {
		this.app = app;
		this.component = component;
		curWord = new StringBuilder();
		popup = new CompletionsPopup();
		popup.addTextField(component);
		sug = new ScrollableSuggestionDisplay();
		seMayLatex = component.getLaTeXSpan();
	}
	/**
	 * Updates curWord to word at current caret position. curWordStart,
	 * curWordEnd are set to this word's start and end position Code copied from
	 * AutoCompleteTextFieldW
	 */
	public void updateCurrentWord(boolean searchRight) {
		int next = updateCurrentWord(searchRight, this.curWord,
		        component.getText(),
		        getCaretPosition());
		if (next > -1) {
			this.curWordStart = next;
		}
	}

	public int getCaretPosition() {
		return geogebra.html5.main.DrawEquationWeb
		        .getCaretPosInEditedValue(seMayLatex);
	}
	


	/**
	 * Code copied from AutoCompleteTextFieldW
	 */
	static int updateCurrentWord(boolean searchRight, StringBuilder curWord,
	        String text, int caretPos) {
		int curWordStart;
		if (text == null)
			return -1;

		if (searchRight) {
			// search to right first to see if we are inside [ ]
			boolean insideBrackets = false;
			curWordStart = caretPos;

			while (curWordStart < text.length()) {
				char c = text.charAt(curWordStart);
				if (c == '[' || c == '(' || c == '{')
					break;
				if (c == ']' || c == ')' || c == '}')
					insideBrackets = true;
				curWordStart++;
			}

			// found [, so go back until we get a ]
			if (insideBrackets) {
				while (caretPos > 0 && text.charAt(caretPos) != '['
				        && text.charAt(caretPos) != '('
				        && text.charAt(caretPos) != '{')
					caretPos--;
			}
		}

		// search to the left
		curWordStart = caretPos - 1;
		while (curWordStart >= 0 &&
		// isLetterOrDigitOrOpenBracket so that F1 works
		        StringUtil.isLetterOrDigitOrUnderscore(text
		                .charAt(curWordStart))) {
			--curWordStart;
		}
		curWordStart++;
		// search to the right
		int curWordEnd = caretPos;
		int length = text.length();
		while (curWordEnd < length
		        && StringUtil.isLetterOrDigitOrUnderscore(text
		                .charAt(curWordEnd)))
			++curWordEnd;

		curWord.setLength(0);
		curWord.append(text.substring(curWordStart, curWordEnd));

		// remove '[' at end
		if (curWord.toString().endsWith("[")
		        || curWord.toString().endsWith("(")
		        || curWord.toString().endsWith("{")) {
			curWord.setLength(curWord.length() - 1);
		}
		return curWordStart;
	}

	public boolean popupSuggestions() {
		// sub, or query is the same as the current word,
		// so moved from method parameter to automatism
		// updateCurrentWord(true);// although true would be nicer here
		updateCurrentWord(false);// compatibility should be preserved
		String sub = this.curWord.toString();
		if (sub != null && !"".equals(sub)) {
			if (sub.length() < 2) {
				// if there is only one letter typed,
				// for any reason, this method should
				// hide the suggestions instead!
				hideSuggestions();
			} else {
				popup.requestSuggestions(new SuggestOracle.Request(sub,
				        querylimit), popupCallback);
			}
		}
		return true;
	}

	public List<String> resetCompletions() {
		String text = component.getText();
		updateCurrentWord(false);
		completions = null;
		// if (isEqualsRequired && !text.startsWith("="))
		// return null;

		String cmdPrefix = curWord.toString();

		// if (korean) {
		// completions = getDictionary().getCompletionsKorean(cmdPrefix);
		// } else {
		completions = getDictionary().getCompletions(cmdPrefix);
		// }

		List<String> commandCompletions = getSyntaxes(completions);

		// Start with the built-in function completions
		completions = app.getParserFunctions().getCompletions(cmdPrefix);
		// Then add the command completions
		if (completions.isEmpty()) {
			completions = commandCompletions;
		} else if (commandCompletions != null) {
			completions.addAll(commandCompletions);
		}
		return completions;
	}

	public AutoCompleteDictionary getDictionary() {
		if (this.dict == null) {
			this.dict = // this.forCAS ? app.getCommandDictionaryCAS() :
			app.getCommandDictionary();
		}
		return dict;
	}

	public List<String> getCompletions() {
		return completions;
	}

	/*
	 * Take a list of commands and return all possible syntaxes for these
	 * commands
	 */
	private List<String> getSyntaxes(List<String> commands) {
		if (commands == null) {
			return null;
		}
		ArrayList<String> syntaxes = new ArrayList<String>();
		for (String cmd : commands) {

			String cmdInt = app.getInternalCommand(cmd);

			String syntaxString;
			// if (isCASInput) {
			// syntaxString = loc.getCommandSyntaxCAS(cmdInt);
			// } else {
			syntaxString = app.getLocalization().getCommandSyntax(cmdInt);
			// }
			if (syntaxString.endsWith(// isCASInput ? Localization.syntaxCAS :
			        Localization.syntaxStr)) {

				// command not found, check for macros
				Macro macro = // isCASInput ? null :
				app.getKernel().getMacro(cmd);
				if (macro != null) {
					syntaxes.add(macro.toString());
				} else {
					// syntaxes.add(cmdInt + "[]");
					App.debug("Can't find syntax for: " + cmd);
				}

				continue;
			}
			for (String syntax : syntaxString.split("\\n")) {
				syntaxes.add(syntax);
			}
		}
		return syntaxes;
	}

	public boolean hideSuggestions() {
		if (sug.isSuggestionListShowing()) {
			sug.hideSuggestions();
		}
		return true;
	}

}
