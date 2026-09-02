#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct Node
{
	char *data;
	struct Node *next;
	struct Node *prev;
} Node;

typedef struct
{
	Node *head;
	Node *tail;
} List;

// Make a copy of the string on the heap and add it to the end of the list.
void insert(List *list, const char *str)
{
	Node *node = malloc(sizeof(Node));
	node->data = malloc(strlen(str) + 1);
	strcpy(node->data, str);
	node->next = NULL;
	node->prev = NULL;

	if (list->head == NULL)
	{
		// The list was empty, so this node is both the head and the tail.
		list->head = node;
		list->tail = node;
	}
	else
	{
		// Attach the node after the current tail.
		node->prev = list->tail;
		list->tail->next = node;
		list->tail = node;
	}
}

// Walk the list from the front and return the first matching node, or NULL.
Node *find(List *list, const char *str)
{
	Node *node = list->head;
	while (node != NULL)
	{
		if (strcmp(node->data, str) == 0)
		{
			return node;
		}
		node = node->next;
	}
	return NULL;
}

// Remove the first node holding [str]. Returns 1 if one was removed, 0 if not.
int delete(List *list, const char *str)
{
	Node *node = find(list, str);
	if (node == NULL)
	{
		return 0;
	}

	if (node->prev == NULL)
	{
		list->head = node->next;
	}
	else
	{
		node->prev->next = node->next;
	}

	if (node->next == NULL)
	{
		list->tail = node->prev;
	}
	else
	{
		node->next->prev = node->prev;
	}

	free(node->data);
	free(node);
	return 1;
}

void print_list(List *list)
{
	Node *node = list->head;
	printf("  list: ");
	while (node != NULL)
	{
		printf("%s ", node->data);
		node = node->next;
	}
	printf("\n");
}

int main(void)
{
	List list = {NULL, NULL};

	insert(&list, "one");
	insert(&list, "two");
	insert(&list, "three");
	insert(&list, "four");
	printf("after inserts:\n");
	print_list(&list);

	printf("\n");
	if (find(&list, "three") != NULL)
	{
		printf("find \"three\": found\n");
	}
	else
	{
		printf("find \"three\": not found\n");
	}

	if (find(&list, "nine") != NULL)
	{
		printf("find \"nine\": found\n");
	}
	else
	{
		printf("find \"nine\": not found\n");
	}

	printf("\ndelete head (one), middle (three), tail (four):\n");
	delete(&list, "one");
	delete(&list, "three");
	delete(&list, "four");
	print_list(&list);

	printf("\ndelete missing (nine) returns %d\n", delete(&list, "nine"));

	delete(&list, "two");
	if (list.head == NULL && list.tail == NULL)
	{
		printf("after deleting the last item, the list is empty\n");
	}

	return 0;
}